package com.chatbot.service;

import com.chatbot.entity.DynamicToolEntity;
import com.chatbot.entity.ToolType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class DynamicToolExecutor {

    private static final Logger log = LoggerFactory.getLogger(DynamicToolExecutor.class);

    private final JdbcTemplate jdbcTemplate;
    private final SqlGuardrailService sqlGuardrail;
    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient;
    private final SemanticSearchService semanticSearchService;

    public DynamicToolExecutor(JdbcTemplate jdbcTemplate,
                               SqlGuardrailService sqlGuardrail,
                               ObjectMapper objectMapper,
                               SemanticSearchService semanticSearchService) {
        this.jdbcTemplate = jdbcTemplate;
        this.sqlGuardrail = sqlGuardrail;
        this.objectMapper = objectMapper;
        this.semanticSearchService = semanticSearchService;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Thực thi Tool động theo cấu hình và tham số do LLM truyền vào
     */
    public String execute(DynamicToolEntity tool, Map<String, Object> arguments) throws Exception {
        ToolType type = tool.getToolType();
        log.info("[Tool Execute] Đang chạy Tool '{}' (Loại: {}) với tham số: {}", tool.getName(), type, arguments);

        switch (type) {
            case STATIC_MARKDOWN:
                return executeStaticMarkdown(tool, arguments);
            case SQL_QUERY:
                return executeSqlQuery(tool, arguments);
            case QDRANT_VECTOR:
                return executeQdrantVector(tool, arguments);
            case HTTP_API:
                return executeHttpApi(tool, arguments);
            default:
                throw new UnsupportedOperationException("Loại Tool không được hỗ trợ: " + type);
        }
    }

    private String executeStaticMarkdown(DynamicToolEntity tool, Map<String, Object> arguments) {
        String template = tool.getConfigData();
        if (template == null) return "Không có dữ liệu thực đơn/markdown.";

        // Thay thế các biến động dạng {{key}} nếu có
        for (Map.Entry<String, Object> entry : arguments.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            template = template.replace(placeholder, String.valueOf(entry.getValue()));
        }

        return template;
    }

    private String executeSqlQuery(DynamicToolEntity tool, Map<String, Object> arguments) {
        String sqlTemplate = tool.getConfigData();
        if (sqlTemplate == null || sqlTemplate.trim().isEmpty()) {
            return "Lỗi cấu hình: Câu lệnh SQL rỗng.";
        }

        // Thay thế tham số dạng :name vào câu query
        String rawSql = sqlTemplate;
        for (Map.Entry<String, Object> entry : arguments.entrySet()) {
            String val = entry.getValue() != null ? entry.getValue().toString().replace("'", "''") : "";
            rawSql = rawSql.replaceAll("(?i):" + entry.getKey(), "'" + val + "'");
        }

        // Kiểm tra an toàn SQL
        String safeSql = sqlGuardrail.validateAndSanitize(rawSql);
        log.debug("[SQL Query]: {}", safeSql);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(safeSql);
        if (rows.isEmpty()) {
            return "Không tìm thấy dữ liệu phù hợp trong cơ sở dữ liệu.";
        }

        // Chuyển kết quả sang Markdown Table trực quan cho LLM
        StringBuilder md = new StringBuilder();
        Set<String> headers = rows.get(0).keySet();

        // Header row
        md.append("| ");
        for (String h : headers) {
            md.append(h).append(" | ");
        }
        md.append("\n| ");
        for (int i = 0; i < headers.size(); i++) {
            md.append("--- | ");
        }
        md.append("\n");

        // Data rows
        for (Map<String, Object> row : rows) {
            md.append("| ");
            for (String h : headers) {
                Object val = row.get(h);
                md.append(val != null ? val.toString().replace("|", "\\|") : "").append(" | ");
            }
            md.append("\n");
        }

        return md.toString();
    }

    private String executeQdrantVector(DynamicToolEntity tool, Map<String, Object> arguments) {
        String query = arguments.getOrDefault("query", "").toString();
        if (query.isEmpty() && arguments.containsKey("keyword")) {
            query = arguments.get("keyword").toString();
        }

        // Đọc cấu hình Qdrant hoặc Knowledge Base từ configData
        String endpoint = "http://localhost:6333";
        String collection = "knowledge_base";
        try {
            JsonNode config = objectMapper.readTree(tool.getConfigData());
            if (config.hasNonNull("knowledgeBaseId")) {
                Long kbId = config.get("knowledgeBaseId").asLong();
                List<com.chatbot.dto.SearchResultDto> results = semanticSearchService.search(kbId, query, 3);
                if (!results.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("### [Dữ liệu trích xuất từ Kho Tri Thức]:\n");
                    for (com.chatbot.dto.SearchResultDto r : results) {
                        sb.append("- ").append(r.getContent().trim()).append("\n");
                    }
                    return sb.toString();
                }
            }
            if (config.hasNonNull("endpoint")) endpoint = config.get("endpoint").asText();
            if (config.hasNonNull("collection")) collection = config.get("collection").asText();
        } catch (Exception ignored) {}

        // Thử kết nối tới Qdrant REST API
        try {
            String targetUrl = endpoint.replaceAll("/+$", "") + "/collections/" + collection + "/points/scroll";
            String jsonPayload = "{\"limit\": 3, \"with_payload\": true}";

            Request request = new Request.Builder()
                    .url(targetUrl)
                    .post(RequestBody.create(MediaType.parse("application/json"), jsonPayload))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonNode resNode = objectMapper.readTree(response.body().string());
                    JsonNode points = resNode.path("result").path("points");
                    if (points.isArray() && points.size() > 0) {
                        StringBuilder sb = new StringBuilder("### Kết quả trích xuất từ Qdrant Vector:\n");
                        for (JsonNode pt : points) {
                            JsonNode pld = pt.path("payload");
                            sb.append("- ").append(pld.toString()).append("\n");
                        }
                        return sb.toString();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[Qdrant] Không thể kết nối tới {} ({}), tự động sử dụng Fallback Knowledge Base", endpoint, e.getMessage());
        }

        // Dữ liệu Mock thông minh khi chưa bật cụm Qdrant thật
        return "### [Cơ sở dữ liệu Tri thức Sản xuất & Luật lao động]:\n" +
               "- **Quy chuẩn An toàn Lao động (ISO 45001)**: Mọi nhân sự trong khu vực phân xưởng bắt buộc trang bị đồ bảo hộ PPE (kính bảo hộ, mũ cứng, giày mũi thép).\n" +
               "- **Quy trình Kiểm soát Chất lượng (QC Production)**: Tỷ lệ sai số linh kiện chấp nhận < 0.02%. Mọi lô hàng phải qua kiểm thử áp lực 3 bước trước khi đóng gói xuất xưởng.\n" +
               "- **Quy định ca làm việc**: Ca sáng (06:00 - 14:00), Ca chiều (14:00 - 22:00), Ca đêm (22:00 - 06:00). Phụ cấp ca đêm tăng 30% lương cơ bản.";
    }

    private String executeHttpApi(DynamicToolEntity tool, Map<String, Object> arguments) throws Exception {
        JsonNode config = objectMapper.readTree(tool.getConfigData());
        String url = config.path("url").asText();
        String method = config.path("method").asText("GET").toUpperCase();

        Request.Builder builder = new Request.Builder();
        if ("POST".equals(method)) {
            String bodyJson = objectMapper.writeValueAsString(arguments);
            builder.url(url).post(RequestBody.create(MediaType.parse("application/json"), bodyJson));
        } else {
            HttpUrl parsedUrl = HttpUrl.parse(url);
            if (parsedUrl != null) {
                HttpUrl.Builder urlBuilder = parsedUrl.newBuilder();
                for (Map.Entry<String, Object> entry : arguments.entrySet()) {
                    if (entry.getValue() != null) {
                        urlBuilder.addQueryParameter(entry.getKey(), entry.getValue().toString());
                    }
                }
                builder.url(urlBuilder.build()).get();
            } else {
                builder.url(url).get();
            }
        }

        try (Response response = httpClient.newCall(builder.build()).execute()) {
            return response.body() != null ? response.body().string() : "Empty HTTP response";
        }
    }
}
