package com.chatbot.service;

import com.chatbot.config.AppProperties;
import com.chatbot.dto.ChatRequest;
import com.chatbot.dto.MessageDto;
import com.chatbot.dto.TitleRequest;
import com.chatbot.dto.TitleResponse;
import com.chatbot.entity.DynamicToolEntity;
import com.chatbot.repository.DynamicToolRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import okio.BufferedSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.net.ssl.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatService.class);

    private static final String SUGGESTIONS_TAG_START = "<<<SUGGESTIONS>>>";
    private static final String SUGGESTIONS_TAG_END = "<<<END_SUGGESTIONS>>>";

    private static final String SUGGESTIONS_SYSTEM_PROMPT = "\n" +
            "QUY TẮC PHẢN HỒI BẮT BUỘC:\n" +
            "1. Luôn tự động nhận diện và phản hồi bằng ĐÚNG NGÔN NGỮ mà người dùng vừa sử dụng trong câu hỏi.\n" +
            "2. Luôn trả lời chi tiết, chính xác, đầy đủ toàn bộ nội dung mà người dùng yêu cầu trước.\n" +
            "3. Ở cuối cùng của câu trả lời, hãy luôn đính kèm khối gợi ý các câu hỏi tiếp theo theo đúng định dạng sau (sử dụng cùng ngôn ngữ với câu trả lời, không viết thêm lời nào sau khối này):\n" +
            "<<<SUGGESTIONS>>>\n" +
            "{\n" +
            "  \"title\": \"Tiêu đề ngắn gợi ý câu hỏi tiếp theo (ví dụ: Bạn muốn tiếp tục tìm hiểu thêm về điều gì?)\",\n" +
            "  \"questions\": [\"Gợi ý câu hỏi 1\", \"Gợi ý câu hỏi 2\", \"Gợi ý câu hỏi 3\"]\n" +
            "}\n" +
            "<<<END_SUGGESTIONS>>>";

    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;
    private final DynamicToolRepository toolRepository;
    private final DynamicToolExecutor toolExecutor;

    @Value("${chatbot.fallback-models:qwen/qwen3.8-27b,openai/gpt-oss-120b,llama-3.3-70b-versatile}")
    private String fallbackModelsConfig;

    private OkHttpClient httpClient;
    private ScheduledExecutorService heartbeatExecutor;

    public AiChatService(AppProperties appProperties,
                         ObjectMapper objectMapper,
                         DynamicToolRepository toolRepository,
                         DynamicToolExecutor toolExecutor) {
        this.appProperties = appProperties;
        this.objectMapper = objectMapper;
        this.toolRepository = toolRepository;
        this.toolExecutor = toolExecutor;
    }

    @PostConstruct
    public void init() {
        this.heartbeatExecutor = Executors.newScheduledThreadPool(4);

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.MINUTES) // Cho phép model reasoning suy nghĩ lâu
                .writeTimeout(60, TimeUnit.SECONDS);

        // Bỏ qua chứng chỉ SSL nếu cấu hình reject-unauthorized=false (giống Node 12)
        if (!appProperties.getSsl().isRejectUnauthorized()) {
            try {
                TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
                            @Override
                            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
                            @Override
                            public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                        }
                };

                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
                builder.hostnameVerifier((hostname, session) -> true);
            } catch (Exception e) {
                log.warn("[AiChatService] Không thể thiết lập SSL bypass: {}", e.getMessage());
            }
        }

        this.httpClient = builder.build();
    }

    /**
     * Xử lý Chat Streaming SSE với Vòng lặp Agent Tool Calling & Multi-model Fallback
     */
    public void streamChat(ChatRequest request, HttpServletResponse response) throws IOException {
        List<MessageDto> messages = request.getMessages();
        if (messages == null || messages.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Tham số `messages` là bắt buộc và phải là mảng không rỗng.");
            return;
        }

        // 1. Inject KnowledgeBase và gợi ý câu hỏi tiếp theo vào system message
        String kb = request.getKnowledgeBase() != null ? request.getKnowledgeBase().trim() : "";
        String kbPrompt = !kb.isEmpty() ? ("\n\n[Cơ sở tri thức (Knowledge Base)]:\n" + kb) : "";

        boolean hasSys = false;
        for (MessageDto m : messages) {
            if ("system".equalsIgnoreCase(m.getRole())) {
                String existing = m.getContent() != null ? String.valueOf(m.getContent()) : "";
                m.setContent(existing + kbPrompt + "\n" + SUGGESTIONS_SYSTEM_PROMPT);
                hasSys = true;
                break;
            }
        }

        if (!hasSys) {
            messages.add(0, new MessageDto("system", "Bạn là trợ lý AI thông minh." + kbPrompt + "\n" + SUGGESTIONS_SYSTEM_PROMPT));
        }

        // Lấy danh sách model thử nghiệm (ưu tiên model user chọn, sau đó là fallback models)
        List<String> candidateModels = buildCandidateModels(request.getModel());

        boolean thinking = Boolean.TRUE.equals(request.getThinking());
        log.info("[Chat Request]: POST /api/chat | Messages: {} | Primary Model: {} | Thinking: {}", messages.size(), candidateModels.get(0), thinking);

        // 2. Lấy danh sách Tool đang hoạt động từ H2 Database
        List<DynamicToolEntity> activeTools = toolRepository.findByEnabledTrue();
        ArrayNode toolsNode = null;
        if (!activeTools.isEmpty()) {
            toolsNode = buildToolsNode(activeTools);
            log.info("[Agent Tools]: Đã kích hoạt {} công cụ từ H2 DB: {}", activeTools.size(),
                    activeTools.stream().map(DynamicToolEntity::getName).toArray());
        }

        // 3. Chuẩn bị response SSE cho client
        response.setContentType("text/event-stream;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-transform");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("X-Accel-Buffering", "no");

        PrintWriter writer = response.getWriter();

        // Heartbeat executor để duy trì kết nối khi AI đang suy nghĩ hoặc chạy tool
        long heartbeatMs = appProperties.getSse().getHeartbeatIntervalMs();
        ScheduledFuture<?> heartbeatTask = heartbeatExecutor.scheduleAtFixedRate(() -> {
            try {
                synchronized (writer) {
                    writer.write(": heartbeat\n\n");
                    writer.flush();
                }
            } catch (Exception ignored) {}
        }, heartbeatMs, heartbeatMs, TimeUnit.MILLISECONDS);

        try {
            // Chạy qua danh sách candidate models (Multi-Model Fallback khi 429/503)
            boolean success = false;
            for (String modelToUse : candidateModels) {
                try {
                    log.info("[LLM Attempt]: Đang gọi mô hình '{}'...", modelToUse);
                    success = executeAgentLoop(modelToUse, messages, toolsNode, activeTools, thinking, writer);
                    if (success) {
                        break;
                    }
                } catch (RateLimitException rle) {
                    log.warn("[Model Failover]: Model '{}' bị Rate Limit (429), chuyển sang model tiếp theo...", modelToUse);
                    sendSseStatus(writer, "failover", "Model " + modelToUse + " bận, đang chuyển sang model dự phòng...");
                }
            }

            if (!success) {
                sendSseData(writer, "{\"choices\": [{\"delta\": {\"content\": \"Xin lỗi, tất cả các mô hình AI hiện tại đều đang quá tải hoặc gặp lỗi kết nối. Vui lòng thử lại sau giây lát.\"}}]}");
            }

            sendSseData(writer, "[DONE]");
        } catch (Exception e) {
            log.error("[Chat Stream Exception]: {}", e.getMessage(), e);
            sendSseData(writer, "{\"error\": \"" + e.getMessage() + "\"}");
            sendSseData(writer, "[DONE]");
        } finally {
            heartbeatTask.cancel(true);
        }
    }

    /**
     * Vòng lặp Agent: Gọi LLM (lần 1) -> Nếu có tool_calls thì thực thi tool và gọi lại LLM (lần 2) stream kết quả
     */
    private boolean executeAgentLoop(String model,
                                     List<MessageDto> messages,
                                     ArrayNode toolsNode,
                                     List<DynamicToolEntity> activeTools,
                                     boolean thinking,
                                     PrintWriter writer) throws Exception {

        // LƯỢT 1: Stream và theo dõi xem model có phát sinh tool_calls không
        ObjectNode payloadNode = createPayload(model, messages, toolsNode, thinking);
        Request request = createOkHttpRequest(payloadNode);

        Map<Integer, ToolCallBuilder> toolCallMap = new HashMap<>();
        StringBuilder streamContentBuffer = new StringBuilder();
        StringBuilder suggestionsRawBuffer = new StringBuilder();
        boolean[] inSuggestions = new boolean[]{false};
        boolean[] hasSentAnyContent = new boolean[]{false};
        boolean[] hasLoggedReasoning = new boolean[]{false};

        try (Response upstreamResponse = httpClient.newCall(request).execute()) {
            if (upstreamResponse.code() == 429 || upstreamResponse.code() == 503) {
                throw new RateLimitException("Upstream HTTP " + upstreamResponse.code());
            }

            if (!upstreamResponse.isSuccessful()) {
                String errBody = upstreamResponse.body() != null ? upstreamResponse.body().string() : "";
                log.error("[Upstream API Error]: Status {} - {}", upstreamResponse.code(), errBody);
                return false;
            }

            ResponseBody body = upstreamResponse.body();
            if (body == null) return false;

            BufferedSource source = body.source();
            while (!source.exhausted()) {
                String line = source.readUtf8Line();
                if (line == null) break;

                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith(":")) continue;
                if (!trimmed.startsWith("data:")) continue;

                String dataStr = trimmed.substring(5).trim();
                if ("[DONE]".equals(dataStr)) break;

                try {
                    JsonNode rootNode = objectMapper.readTree(dataStr);
                    JsonNode choices = rootNode.get("choices");
                    if (choices != null && choices.isArray() && choices.size() > 0) {
                        JsonNode choice = choices.get(0);
                        JsonNode delta = choice.get("delta");
                        if (delta != null) {
                            // A. Thu thập tool_calls nếu model quyết định gọi Tool
                            if (delta.hasNonNull("tool_calls") && delta.get("tool_calls").isArray()) {
                                for (JsonNode tc : delta.get("tool_calls")) {
                                    int idx = tc.hasNonNull("index") ? tc.get("index").asInt() : 0;
                                    ToolCallBuilder tcb = toolCallMap.computeIfAbsent(idx, k -> new ToolCallBuilder());
                                    if (tc.hasNonNull("id")) tcb.id = tc.get("id").asText();
                                    JsonNode fn = tc.get("function");
                                    if (fn != null) {
                                        if (fn.hasNonNull("name")) tcb.name.append(fn.get("name").asText());
                                        if (fn.hasNonNull("arguments")) tcb.arguments.append(fn.get("arguments").asText());
                                    }
                                }
                                continue;
                            }

                            // B. Forward reasoning tokens
                            String reasoning = null;
                            if (delta.hasNonNull("reasoning")) {
                                reasoning = delta.get("reasoning").asText();
                            } else if (delta.hasNonNull("reasoning_content")) {
                                reasoning = delta.get("reasoning_content").asText();
                            }

                            if (reasoning != null && !reasoning.isEmpty()) {
                                if (!hasLoggedReasoning[0]) {
                                    hasLoggedReasoning[0] = true;
                                    log.info("[Thinking]: LLM đang truyền reasoning tokens về client...");
                                }
                                emitReasoning(writer, reasoning);
                            }

                            // C. Xử lý content delta (tách khối <<<SUGGESTIONS>>>)
                            if (delta.hasNonNull("content")) {
                                String contentDelta = delta.get("content").asText();
                                handleContentDelta(contentDelta, streamContentBuffer, suggestionsRawBuffer, inSuggestions, hasSentAnyContent, writer);
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }
        }

        // NẾU CÓ TOOL CALLS ĐƯỢC KÍCH HOẠT: THỰC THI TOOLS VÀ GỌI LƯỢT 2 TỔNG HỢP
        if (!toolCallMap.isEmpty()) {
            log.info("[Agent Loop]: Phát hiện {} Tool Call yêu cầu từ LLM.", toolCallMap.size());

            // Lưu assistant message với tool_calls vào context
            ArrayNode assistantToolCalls = objectMapper.createArrayNode();
            for (ToolCallBuilder tcb : toolCallMap.values()) {
                if (tcb.id == null || tcb.id.trim().isEmpty()) {
                    tcb.id = "call_" + UUID.randomUUID().toString().replace("-", "").substring(0, 9);
                }
                ObjectNode tcNode = assistantToolCalls.addObject();
                tcNode.put("id", tcb.id);
                tcNode.put("type", "function");
                ObjectNode fnNode = tcNode.putObject("function");
                fnNode.put("name", tcb.name.toString());
                fnNode.put("arguments", tcb.arguments.toString());
            }

            MessageDto assistantMsg = new MessageDto("assistant", null);
            assistantMsg.setToolCalls(assistantToolCalls);
            messages.add(assistantMsg);

            // Duyệt và thực thi từng Tool Call
            for (ToolCallBuilder tcb : toolCallMap.values()) {
                String toolName = tcb.name.toString();
                String argsJson = tcb.arguments.toString();
                log.info("[Tool Call]: Thực thi '{}' (id: {}) với tham số: {}", toolName, tcb.id, argsJson);

                // Stream trạng thái "tool_start" trực quan về frontend
                sendSseStatus(writer, "tool_start", "Đang truy xuất dữ liệu từ công cụ: " + toolName + "...");

                DynamicToolEntity matchedTool = activeTools.stream()
                        .filter(t -> t.getName().equalsIgnoreCase(toolName))
                        .findFirst()
                        .orElse(null);

                String toolResult;
                if (matchedTool != null) {
                    try {
                        Map<String, Object> argsMap = parseArgsJson(argsJson);
                        toolResult = toolExecutor.execute(matchedTool, argsMap);
                    } catch (Exception ex) {
                        log.error("[Tool Error]: Lỗi thực thi {}: {}", toolName, ex.getMessage());
                        toolResult = "Lỗi khi chạy công cụ " + toolName + ": " + ex.getMessage();
                    }
                } else {
                    toolResult = "Không tìm thấy công cụ tên '" + toolName + "' trong hệ thống.";
                }

                // Stream trạng thái "tool_done"
                sendSseStatus(writer, "tool_done", "Đã lấy dữ liệu từ " + toolName + " thành công.");

                // Thêm kết quả tool vào messages với tool_call_id khớp chính xác!
                MessageDto toolMsg = new MessageDto("tool", toolResult, tcb.id, toolName);
                messages.add(toolMsg);
            }

            // LƯỢT 2: Gọi LLM tổng hợp câu trả lời cuối cùng từ dữ liệu Tool
            log.info("[Agent Loop]: Lượt 2 - Gửi dữ liệu Tool cho LLM để stream câu trả lời hoàn chỉnh...");
            ObjectNode secondPayload = createPayload(model, messages, null, thinking);
            Request secondRequest = createOkHttpRequest(secondPayload);

            return streamResponseChunks(secondRequest, writer);
        }

        // NẾU KHÔNG CÓ TOOL CALLS: Hoàn tất stream lượt 1
        if (streamContentBuffer.length() > 0 && !inSuggestions[0]) {
            hasSentAnyContent[0] = true;
            sendContentChunk(writer, streamContentBuffer.toString());
            streamContentBuffer.setLength(0);
        }

        if (suggestionsRawBuffer.length() > 0) {
            parseAndEmitSuggestions(suggestionsRawBuffer.toString(), writer);
        }

        return true;
    }

    /**
     * Stream trực tiếp các chunk từ request lượt 2 về client
     */
    private boolean streamResponseChunks(Request request, PrintWriter writer) throws Exception {
        StringBuilder streamContentBuffer = new StringBuilder();
        StringBuilder suggestionsRawBuffer = new StringBuilder();
        boolean[] inSuggestions = new boolean[]{false};
        boolean[] hasSentAnyContent = new boolean[]{false};
        boolean[] hasLoggedReasoning = new boolean[]{false};

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) return false;
            ResponseBody body = response.body();
            if (body == null) return false;

            BufferedSource source = body.source();
            while (!source.exhausted()) {
                String line = source.readUtf8Line();
                if (line == null) break;

                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith(":")) continue;
                if (!trimmed.startsWith("data:")) continue;

                String dataStr = trimmed.substring(5).trim();
                if ("[DONE]".equals(dataStr)) break;

                try {
                    JsonNode rootNode = objectMapper.readTree(dataStr);
                    JsonNode choices = rootNode.get("choices");
                    if (choices != null && choices.isArray() && choices.size() > 0) {
                        JsonNode choice = choices.get(0);
                        JsonNode delta = choice.get("delta");
                        if (delta != null) {
                            // Forward reasoning
                            String reasoning = delta.hasNonNull("reasoning") ? delta.get("reasoning").asText()
                                    : (delta.hasNonNull("reasoning_content") ? delta.get("reasoning_content").asText() : null);

                            if (reasoning != null && !reasoning.isEmpty()) {
                                if (!hasLoggedReasoning[0]) {
                                    hasLoggedReasoning[0] = true;
                                }
                                emitReasoning(writer, reasoning);
                            }

                            // Forward text
                            if (delta.hasNonNull("content")) {
                                handleContentDelta(delta.get("content").asText(), streamContentBuffer, suggestionsRawBuffer, inSuggestions, hasSentAnyContent, writer);
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }

            if (streamContentBuffer.length() > 0 && !inSuggestions[0]) {
                hasSentAnyContent[0] = true;
                sendContentChunk(writer, streamContentBuffer.toString());
            }

            if (suggestionsRawBuffer.length() > 0) {
                parseAndEmitSuggestions(suggestionsRawBuffer.toString(), writer);
            }

            return true;
        }
    }

    private ObjectNode createPayload(String model, List<MessageDto> messages, ArrayNode toolsNode, boolean thinking) {
        ObjectNode payloadNode = objectMapper.createObjectNode();
        payloadNode.put("model", model);
        payloadNode.set("messages", objectMapper.valueToTree(messages));
        payloadNode.put("temperature", appProperties.getAi().getTemperature());
        payloadNode.put("max_tokens", appProperties.getAi().getMaxTokens());
        payloadNode.put("max_completion_tokens", appProperties.getAi().getMaxTokens());
        payloadNode.put("top_p", appProperties.getAi().getTopP());
        payloadNode.put("stream", true);

        if (thinking) {
            payloadNode.put("reasoning_format", "parsed");
        } else {
            payloadNode.put("reasoning_format", "hidden");
        }

        if (toolsNode != null && toolsNode.size() > 0) {
            payloadNode.set("tools", toolsNode);
            payloadNode.put("tool_choice", "auto");
        }

        return payloadNode;
    }

    private Request createOkHttpRequest(ObjectNode payloadNode) throws IOException {
        String requestBodyJson = objectMapper.writeValueAsString(payloadNode);
        String apiUrl = appProperties.getOpenai().getApiUrl().trim();
        String apiKey = appProperties.getOpenai().getApiKey() != null ? appProperties.getOpenai().getApiKey().trim() : "";

        Request.Builder reqBuilder = new Request.Builder()
                .url(apiUrl)
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestBodyJson));

        if (!apiKey.isEmpty()) {
            reqBuilder.header("Authorization", "Bearer " + apiKey);
        }

        return reqBuilder.build();
    }

    private ArrayNode buildToolsNode(List<DynamicToolEntity> tools) {
        ArrayNode arr = objectMapper.createArrayNode();
        for (DynamicToolEntity tool : tools) {
            try {
                ObjectNode item = arr.addObject();
                item.put("type", "function");
                ObjectNode fn = item.putObject("function");
                fn.put("name", tool.getName());
                fn.put("description", tool.getDescription());
                fn.set("parameters", objectMapper.readTree(tool.getParametersSchema()));
            } catch (Exception e) {
                log.warn("[Tool Schema Error] Bỏ qua tool '{}' do schema lỗi: {}", tool.getName(), e.getMessage());
            }
        }
        return arr;
    }

    private List<String> buildCandidateModels(String requestedModel) {
        List<String> list = new ArrayList<>();
        if (requestedModel != null && !requestedModel.trim().isEmpty()) {
            list.add(requestedModel.trim());
        } else {
            list.add(appProperties.getOpenai().getDefaultModel());
        }

        if (fallbackModelsConfig != null) {
            for (String m : fallbackModelsConfig.split(",")) {
                String clean = m.trim();
                if (!clean.isEmpty() && !list.contains(clean)) {
                    list.add(clean);
                }
            }
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseArgsJson(String json) {
        if (json == null || json.trim().isEmpty()) return Collections.emptyMap();
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private void emitReasoning(PrintWriter writer, String reasoning) {
        try {
            ObjectNode rNode = objectMapper.createObjectNode();
            ArrayNode cArr = rNode.putArray("choices");
            ObjectNode cObj = cArr.addObject();
            ObjectNode dObj = cObj.putObject("delta");
            dObj.put("reasoning", reasoning);
            sendSseData(writer, objectMapper.writeValueAsString(rNode));
        } catch (Exception ignored) {}
    }

    private void handleContentDelta(String contentDelta,
                                    StringBuilder streamContentBuffer,
                                    StringBuilder suggestionsRawBuffer,
                                    boolean[] inSuggestions,
                                    boolean[] hasSentAnyContent,
                                    PrintWriter writer) {
        if (inSuggestions[0]) {
            suggestionsRawBuffer.append(contentDelta);
            return;
        }

        streamContentBuffer.append(contentDelta);
        String currentStr = streamContentBuffer.toString();
        int tagIdx = currentStr.indexOf(SUGGESTIONS_TAG_START);

        if (tagIdx != -1) {
            String normalPart = currentStr.substring(0, tagIdx);
            if (!normalPart.isEmpty()) {
                hasSentAnyContent[0] = true;
                sendContentChunk(writer, normalPart);
            }
            inSuggestions[0] = true;
            suggestionsRawBuffer.append(currentStr.substring(tagIdx + SUGGESTIONS_TAG_START.length()));
            streamContentBuffer.setLength(0);
        } else {
            int matchLen = 0;
            int maxCheck = Math.min(currentStr.length(), SUGGESTIONS_TAG_START.length() - 1);
            for (int len = maxCheck; len >= 1; len--) {
                String suffix = currentStr.substring(currentStr.length() - len);
                if (SUGGESTIONS_TAG_START.startsWith(suffix)) {
                    matchLen = len;
                    break;
                }
            }

            int safeLength = currentStr.length() - matchLen;
            if (safeLength > 0) {
                String toSend = currentStr.substring(0, safeLength);
                hasSentAnyContent[0] = true;
                sendContentChunk(writer, toSend);
                streamContentBuffer.delete(0, safeLength);
            }
        }
    }

    private void parseAndEmitSuggestions(String rawSuggestions, PrintWriter writer) {
        try {
            String clean = rawSuggestions;
            int endIdx = clean.indexOf(SUGGESTIONS_TAG_END);
            if (endIdx != -1) {
                clean = clean.substring(0, endIdx);
            }
            clean = clean.replaceAll("^```(?:json)?\\s*|```$", "").trim();

            String title = "";
            List<String> questions = new ArrayList<>();

            try {
                JsonNode parsed = objectMapper.readTree(clean);
                if (parsed.hasNonNull("title")) {
                    title = parsed.get("title").asText().trim();
                }
                if (parsed.hasNonNull("questions") && parsed.get("questions").isArray()) {
                    for (JsonNode qNode : parsed.get("questions")) {
                        String q = qNode.asText().trim();
                        if (!q.isEmpty()) questions.add(q);
                    }
                }
            } catch (Exception ex) {
                Matcher tm = Pattern.compile("\"title\"\\s*:\\s*\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"").matcher(clean);
                if (tm.find()) title = tm.group(1).trim();
                Matcher qm = Pattern.compile("\"questions\"\\s*:\\s*\\[([\\s\\S]*?)\\]").matcher(clean);
                if (qm.find()) {
                    Matcher itemMatcher = Pattern.compile("\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"").matcher(qm.group(1));
                    while (itemMatcher.find()) {
                        String q = itemMatcher.group(1).trim();
                        if (!q.isEmpty()) questions.add(q);
                    }
                }
            }

            if (!questions.isEmpty()) {
                if (questions.size() > 3) questions = questions.subList(0, 3);
                ObjectNode sNode = objectMapper.createObjectNode();
                ArrayNode qArr = sNode.putArray("__suggestions__");
                for (String q : questions) qArr.add(q);
                sNode.put("__suggestions_title__", title);

                log.info("[Tag Suggestions] Tiêu đề: {} | Gợi ý: {}", title.isEmpty() ? "(Mặc định)" : title, questions);
                sendSseData(writer, objectMapper.writeValueAsString(sNode));
            }
        } catch (Exception e) {
            log.error("[Tag Suggestions Parse Error]: {}", e.getMessage());
        }
    }

    private void sendContentChunk(PrintWriter writer, String content) {
        try {
            ObjectNode rNode = objectMapper.createObjectNode();
            ArrayNode cArr = rNode.putArray("choices");
            ObjectNode cObj = cArr.addObject();
            ObjectNode dObj = cObj.putObject("delta");
            dObj.put("content", content);
            sendSseData(writer, objectMapper.writeValueAsString(rNode));
        } catch (Exception ignored) {}
    }

    private void sendSseStatus(PrintWriter writer, String status, String message) {
        try {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("__status__", status);
            node.put("__message__", message);
            sendSseData(writer, objectMapper.writeValueAsString(node));
        } catch (Exception ignored) {}
    }

    /**
     * Tự động sinh tiêu đề cho phiên chat kèm 1 emoji ở đầu
     */
    public TitleResponse generateTitle(TitleRequest request) {
        String userText = "";
        if (request.getContent() != null && !request.getContent().trim().isEmpty()) {
            userText = request.getContent().trim();
        } else if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (MessageDto m : request.getMessages()) {
                if (m.getContent() instanceof String) {
                    String str = ((String) m.getContent()).trim();
                    if (!str.isEmpty()) {
                        sb.append(m.getRole()).append(": ").append(str).append("\n");
                    }
                }
            }
            userText = sb.toString().trim();
        }

        if (userText.isEmpty()) {
            return new TitleResponse("💬 Cuộc trò chuyện mới", "💬");
        }

        if (userText.length() > 600) {
            userText = userText.substring(0, 600);
        }

        List<String> candidates = buildCandidateModels(request.getModel());

        String prompt = "Nhiệm vụ: Dựa vào nội dung hội thoại sau, hãy đặt đúng 1 tiêu đề thật ngắn gọn (từ 2 đến 5 từ tiếng Việt).\n" +
                "QUY TẮC:\n" +
                "1. BẮT BUỘC bắt đầu bằng ĐÚNG 1 emoji phù hợp nhất với chủ đề, theo sau là 1 khoảng trắng và tiêu đề.\n" +
                "2. KHÔNG viết trong dấu ngoặc kép, KHÔNG giải thích, KHÔNG thêm tiền tố như 'Tiêu đề:'. Chỉ trả về đúng 1 dòng duy nhất gồm emoji và tiêu đề.\n" +
                "Ví dụ:\n" +
                "🐍 Lập trình Python\n" +
                "🛒 Thiết kế Database E-Commerce\n" +
                "🎨 Thiết kế Giao diện\n" +
                "🚀 Kế hoạch Khởi nghiệp\n\n" +
                "Nội dung hội thoại:\n" + userText;

        for (String model : candidates) {
            try {
                ObjectNode payloadNode = objectMapper.createObjectNode();
                payloadNode.put("model", model);
                ArrayNode msgs = payloadNode.putArray("messages");
                ObjectNode mNode = msgs.addObject();
                mNode.put("role", "user");
                mNode.put("content", prompt);
                payloadNode.put("temperature", 0.3);
                payloadNode.put("max_tokens", 40);
                payloadNode.put("stream", false);

                Request okRequest = createOkHttpRequest(payloadNode);
                try (Response res = httpClient.newCall(okRequest).execute()) {
                    if (res.isSuccessful() && res.body() != null) {
                        String bodyStr = res.body().string();
                        JsonNode root = objectMapper.readTree(bodyStr);
                        JsonNode choices = root.get("choices");
                        if (choices != null && choices.size() > 0) {
                            JsonNode msg = choices.get(0).get("message");
                            if (msg != null && msg.hasNonNull("content")) {
                                String rawTitle = msg.get("content").asText().trim();
                                TitleResponse tr = parseTitleResult(rawTitle, userText);
                                if (tr != null) {
                                    log.info("[Title Generation] Sinh tiêu đề thành công bằng model {}: {}", model, tr.getTitle());
                                    return tr;
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("[Title Generation] Model {} sinh tiêu đề không thành công: {}", model, e.getMessage());
            }
        }

        return fallbackTitleFromContent(userText);
    }

    private TitleResponse parseTitleResult(String rawTitle, String userText) {
        if (rawTitle == null || rawTitle.trim().isEmpty()) {
            return null;
        }
        String clean = rawTitle.replaceAll("[\"'\r\n`*#]", " ").trim();
        clean = clean.replaceAll("^(?i)(tiêu\\s*đề|title|chủ\\s*đề)[:\\s-]+", "").trim();
        if (clean.isEmpty()) return null;

        // Kiểm tra xem ký tự đầu tiên có phải emoji không (xét surrogate pair hoặc symbol)
        if (clean.length() >= 2 && Character.isSurrogatePair(clean.charAt(0), clean.charAt(1))) {
            String emoji = clean.substring(0, 2);
            String titlePart = clean.substring(2).trim();
            if (!titlePart.isEmpty()) {
                if (titlePart.length() > 36) titlePart = titlePart.substring(0, 36) + "...";
                return new TitleResponse(emoji + " " + titlePart, emoji);
            }
        } else if (clean.length() >= 1 && (clean.charAt(0) >= 0x2600 && clean.charAt(0) <= 0x27BF)) {
            String emoji = clean.substring(0, 1);
            String titlePart = clean.substring(1).trim();
            if (!titlePart.isEmpty()) {
                if (titlePart.length() > 36) titlePart = titlePart.substring(0, 36) + "...";
                return new TitleResponse(emoji + " " + titlePart, emoji);
            }
        }

        String emoji = detectEmojiFromText(clean + " " + userText);
        if (clean.length() > 36) clean = clean.substring(0, 36) + "...";
        return new TitleResponse(emoji + " " + clean, emoji);
    }

    private String detectEmojiFromText(String text) {
        String lower = text.toLowerCase();
        if (lower.contains("code") || lower.contains("python") || lower.contains("java") || lower.contains("bug") || lower.contains("lỗi") || lower.contains("html") || lower.contains("css") || lower.contains("sql") || lower.contains("git")) {
            return "💻";
        }
        if (lower.contains("chào") || lower.contains("hello") || lower.contains("hi") || lower.contains("alo")) {
            return "👋";
        }
        if (lower.contains("ảnh") || lower.contains("image") || lower.contains("hình") || lower.contains("photo")) {
            return "🖼️";
        }
        if (lower.contains("tiền") || lower.contains("giá") || lower.contains("kinh doanh") || lower.contains("bán") || lower.contains("mua") || lower.contains("finance")) {
            return "💰";
        }
        if (lower.contains("học") || lower.contains("sách") || lower.contains("nghiên cứu") || lower.contains("giải thích") || lower.contains("toán")) {
            return "📚";
        }
        if (lower.contains("nhạc") || lower.contains("hát") || lower.contains("bài hát") || lower.contains("âm nhạc")) {
            return "🎵";
        }
        if (lower.contains("game") || lower.contains("chơi")) {
            return "🎮";
        }
        if (lower.contains("sức khỏe") || lower.contains("bệnh") || lower.contains("thuốc") || lower.contains("bác sĩ")) {
            return "🏥";
        }
        if (lower.contains("ăn") || lower.contains("nấu") || lower.contains("món") || lower.contains("bếp")) {
            return "🍳";
        }
        if (lower.contains("du lịch") || lower.contains("vé") || lower.contains("khách sạn") || lower.contains("bay")) {
            return "✈️";
        }
        return "💬";
    }

    private TitleResponse fallbackTitleFromContent(String text) {
        String emoji = detectEmojiFromText(text);
        String snippet = text.replaceAll("\\s+", " ").trim();
        if (snippet.length() > 28) {
            snippet = snippet.substring(0, 28) + "...";
        }
        return new TitleResponse(emoji + " " + snippet, emoji);
    }

    private void sendSseData(PrintWriter writer, String data) {
        synchronized (writer) {
            writer.write("data: " + data + "\n\n");
            writer.flush();
        }
    }

    private static class ToolCallBuilder {
        String id;
        StringBuilder name = new StringBuilder();
        StringBuilder arguments = new StringBuilder();
    }

    private static class RateLimitException extends Exception {
        public RateLimitException(String message) {
            super(message);
        }
    }
}
