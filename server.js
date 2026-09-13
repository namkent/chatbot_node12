/**
 * Server API Service cho UrChatbot Widget
 * Chuẩn kết nối: OpenAI Compatible Streaming Chat API
 * Chạy độc lập và tương thích hoàn toàn trên Node.js 12+ (CommonJS, ES2019).
 *
 * Nhận cấu hình từ file .env:
 * - PORT: Cổng chạy service (mặc định: 3001)
 * - OPENAI_API_URL: URL chat completions upstream (Groq, OpenRouter, OpenAI, Ollama, vLLM, DeepSeek...)
 * - OPENAI_API_KEY: API Key tương ứng
 * - OPENAI_MODEL: Tên model AI mặc định
 * - AI_TEMPERATURE: (tùy chọn, mặc định 0.6)
 * - AI_MAX_TOKENS: (tùy chọn, mặc định 8192)
 * - AI_TOP_P: (tùy chọn, mặc định 0.95)
 * - NODE_TLS_REJECT_UNAUTHORIZED: (tùy chọn, đặt 0 để bỏ qua lỗi SSL certificate chain trên Node 12)
 *
 * Endpoint:
 * - POST /api/chat: stream SSE + tool_call suggest_followup_questions + inject knowledgeBase
 */

// 1. Cố gắng nạp dotenv nếu package có sẵn
try {
  require('dotenv').config();
} catch (e) {
  // Tiếp tục với loadEnv thuần nếu dotenv chưa cài đặt
}

const http = require('http');
const https = require('https');
const url = require('url');
const fs = require('fs');
const path = require('path');
const { StringDecoder } = require('string_decoder');

// 2. Hàm đọc và phân tích file .env thuần (tương thích Node 12, không cần dependency)
function loadEnv() {
  const envCandidates = [
    path.resolve(__dirname, '.env'),
    path.resolve(process.cwd(), '.env')
  ];

  for (let i = 0; i < envCandidates.length; i++) {
    const envPath = envCandidates[i];
    if (fs.existsSync(envPath)) {
      try {
        let content = fs.readFileSync(envPath, 'utf8');
        // Loại bỏ UTF-8 BOM nếu có
        content = content.replace(/^\uFEFF/, '');
        const lines = content.split(/\r?\n/);

        for (let j = 0; j < lines.length; j++) {
          const line = lines[j].trim();
          if (!line || line.indexOf('#') === 0) continue;

          const eqIdx = line.indexOf('=');
          if (eqIdx !== -1) {
            const key = line.slice(0, eqIdx).trim();
            let val = line.slice(eqIdx + 1).trim();

            // Xử lý bọc nháy kép hoặc nháy đơn
            if (val.indexOf('"') === 0 && val.lastIndexOf('"') > 0) {
              val = val.slice(1, val.lastIndexOf('"'));
            } else if (val.indexOf("'") === 0 && val.lastIndexOf("'") > 0) {
              val = val.slice(1, val.lastIndexOf("'"));
            } else {
              // Bỏ comment inline nếu có (dạng KEY=VALUE # comment)
              const hashIdx = val.indexOf('#');
              if (hashIdx !== -1) {
                val = val.slice(0, hashIdx).trim();
              }
            }

            // Gán vào process.env nếu chưa tồn tại hoặc rỗng
            if (process.env[key] === undefined || process.env[key] === '') {
              process.env[key] = val;
            }
          }
        }
      } catch (err) {
        console.warn('[Env] Không thể đọc file .env:', err.message);
      }
      break;
    }
  }
}

loadEnv();

// Cấu hình từ môi trường / .env
const PORT = parseInt(process.env.PORT, 10) || 3001;
const OPENAI_API_URL = (process.env.OPENAI_API_URL || 'https://api.groq.com/openai/v1/chat/completions').trim();
const OPENAI_API_KEY = (process.env.OPENAI_API_KEY || '').trim();
const DEFAULT_MODEL = (process.env.OPENAI_MODEL || 'qwen/qwen3.8-27b').trim();

// Cấu hình tham số sinh văn bản AI
const FIXED_TEMPERATURE = parseFloat(process.env.AI_TEMPERATURE) || 0.6;
const FIXED_MAX_TOKENS = parseInt(process.env.AI_MAX_TOKENS, 10) || 8192;
const FIXED_TOP_P = parseFloat(process.env.AI_TOP_P) || 0.95;

// Heartbeat interval (ms) - gửi comment SSE để duy trì kết nối khi model suy nghĩ lâu
const HEARTBEAT_INTERVAL_MS = parseInt(process.env.HEARTBEAT_INTERVAL_MS, 10) || 15000;

// Cấu hình MinIO S3 Compatible Service
const MINIO_ENDPOINT = (process.env.MINIO_ENDPOINT || 'http://127.0.0.1:9000').trim().replace(/\/+$/, '');
const MINIO_ACCESS_KEY = (process.env.MINIO_ACCESS_KEY || 'minioadmin').trim();
const MINIO_SECRET_KEY = (process.env.MINIO_SECRET_KEY || 'minioadmin').trim();
const MINIO_BUCKET = (process.env.MINIO_BUCKET || 'chatbot').trim();
const MINIO_PUBLIC_URL = (process.env.MINIO_PUBLIC_URL || (MINIO_ENDPOINT + '/' + MINIO_BUCKET)).trim().replace(/\/+$/, '');

// Xử lý SSL trên Node 12: Do các chứng chỉ CA tích hợp trong Node 12 đã cũ (như Let's Encrypt hết hạn)
// hoặc trên Windows có proxy/antivirus can thiệp SSL, mặc định sẽ bỏ qua kiểm tra chứng chỉ trừ khi đặt NODE_TLS_REJECT_UNAUTHORIZED=1
const REJECT_UNAUTHORIZED = process.env.NODE_TLS_REJECT_UNAUTHORIZED === '1';

// Định nghĩa thẻ tag cho gợi ý câu hỏi tiếp theo
const SUGGESTIONS_TAG_START = '<<<SUGGESTIONS>>>';
const SUGGESTIONS_TAG_END = '<<<END_SUGGESTIONS>>>';

// Hướng dẫn gợi ý câu hỏi tiếp theo đưa vào system prompt
const SUGGESTIONS_SYSTEM_PROMPT = `
QUY TẮC PHẢN HỒI BẮT BUỘC:
1. Luôn tự động nhận diện và phản hồi bằng ĐÚNG NGÔN NGỮ mà người dùng vừa sử dụng trong câu hỏi.
2. Luôn trả lời chi tiết, chính xác, đầy đủ toàn bộ nội dung mà người dùng yêu cầu trước.
3. Ở cuối cùng của câu trả lời, hãy luôn đính kèm khối gợi ý các câu hỏi tiếp theo theo đúng định dạng sau (sử dụng cùng ngôn ngữ với câu trả lời, không viết thêm lời nào sau khối này):
<<<SUGGESTIONS>>>
{
  "title": "Tiêu đề ngắn gợi ý câu hỏi tiếp theo (ví dụ: Bạn muốn tiếp tục tìm hiểu thêm về điều gì?)",
  "questions": ["Gợi ý câu hỏi 1", "Gợi ý câu hỏi 2", "Gợi ý câu hỏi 3"]
}
<<<END_SUGGESTIONS>>>`;

// Tool definition dự phòng cho gợi ý câu hỏi tiếp theo (nếu có model yêu cầu)
const SUGGEST_TOOL = {
  type: 'function',
  function: {
    name: 'suggest_followup_questions',
    description: 'Generate an engaging follow-up title question (not clickable) and 2-3 short, relevant follow-up options (buttons) based on the conversation. Always use the SAME LANGUAGE as the user\'s last message.',
    parameters: {
      type: 'object',
      properties: {
        title: {
          type: 'string',
          description: 'A context-relevant introductory question or prompt heading asking what the user wants to do or explore next (e.g., "Bạn muốn tiến hành bước nào tiếp theo?" or "Bạn có muốn tìm hiểu thêm về các nội dung sau không?"). In the same language as user.'
        },
        questions: {
          type: 'array',
          description: 'List of 2-3 short follow-up action/question choices in the same language as the user',
          items: { type: 'string' },
          minItems: 2,
          maxItems: 3
        }
      },
      required: ['title', 'questions']
    }
  }
};

// Hàm ẩn bớt ký tự API key khi in log bảo mật
function maskKey(key) {
  if (!key) return '(Chưa cấu hình)';
  if (key.length <= 10) return '******';
  return key.slice(0, 6) + '...' + key.slice(-4);
}

// Hàm gửi phản hồi JSON chuẩn có hỗ trợ CORS
function sendJson(res, statusCode, data) {
  res.writeHead(statusCode, {
    'Content-Type': 'application/json; charset=utf-8',
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Methods': 'GET, POST, DELETE, OPTIONS',
    'Access-Control-Allow-Headers': 'Content-Type, Authorization'
  });
  res.end(JSON.stringify(data));
}

// Đọc nội dung body của request (mặc định hỗ trợ đến 25MB cho ảnh tải lên)
function getRequestBody(req, maxLimit) {
  const limit = maxLimit || (25 * 1024 * 1024);
  return new Promise(function (resolve, reject) {
    let body = '';
    req.on('data', function (chunk) {
      body += chunk;
      if (body.length > limit) {
        reject(new Error('Payload too large (vượt quá giới hạn ' + Math.round(limit / (1024 * 1024)) + 'MB)'));
      }
    });
    req.on('end', function () {
      try {
        resolve(body ? JSON.parse(body) : {});
      } catch (err) {
        reject(new Error('Invalid JSON format: ' + err.message));
      }
    });
    req.on('error', reject);
  });
}

// Hàm upload buffer trực tiếp lên MinIO S3 bucket (tương thích Node 12)
function uploadToMinio(key, buffer, contentType) {
  return new Promise(function (resolve, reject) {
    const targetUrl = new url.URL(MINIO_ENDPOINT + '/' + MINIO_BUCKET + '/' + encodeURIComponent(key));
    const isHttps = targetUrl.protocol === 'https:';
    const client = isHttps ? https : http;

    const options = {
      protocol: targetUrl.protocol,
      hostname: targetUrl.hostname,
      port: targetUrl.port || (isHttps ? 443 : 80),
      path: targetUrl.pathname + targetUrl.search,
      method: 'PUT',
      headers: {
        'Content-Type': contentType || 'application/octet-stream',
        'Content-Length': buffer.length
      }
    };

    const req = client.request(options, function (res) {
      let body = '';
      res.on('data', function (chunk) { body += chunk; });
      res.on('end', function () {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve({
            success: true,
            key: key,
            url: MINIO_PUBLIC_URL + '/' + encodeURIComponent(key),
            size: buffer.length,
            mimeType: contentType
          });
        } else {
          reject(new Error('MinIO PUT failed status ' + res.statusCode + ': ' + body));
        }
      });
    });

    req.on('error', reject);
    req.write(buffer);
    req.end();
  });
}

// Hàm xóa file khỏi MinIO S3 bucket
function deleteFromMinio(key) {
  return new Promise(function (resolve, reject) {
    // Nếu key là đường dẫn đầy đủ, lấy phần filename cuối cùng
    const cleanKey = key ? key.replace(/^.*[\\\/]/, '').trim() : '';
    if (!cleanKey) {
      return resolve({ success: true, message: 'Key rỗng, bỏ qua' });
    }

    const targetUrl = new url.URL(MINIO_ENDPOINT + '/' + MINIO_BUCKET + '/' + encodeURIComponent(cleanKey));
    const isHttps = targetUrl.protocol === 'https:';
    const client = isHttps ? https : http;

    const options = {
      protocol: targetUrl.protocol,
      hostname: targetUrl.hostname,
      port: targetUrl.port || (isHttps ? 443 : 80),
      path: targetUrl.pathname + targetUrl.search,
      method: 'DELETE'
    };

    const req = client.request(options, function (res) {
      let body = '';
      res.on('data', function (chunk) { body += chunk; });
      res.on('end', function () {
        // Status 200, 204 hoặc 404 (file đã xóa hoặc không tồn tại) đều xem là thành công
        if ((res.statusCode >= 200 && res.statusCode < 300) || res.statusCode === 404) {
          resolve({ success: true, key: cleanKey });
        } else {
          reject(new Error('MinIO DELETE failed status ' + res.statusCode + ': ' + body));
        }
      });
    });

    req.on('error', reject);
    req.end();
  });
}

const server = http.createServer(async function (req, res) {
  // 1. CORS Preflight
  if (req.method === 'OPTIONS') {
    res.writeHead(204, {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, POST, DELETE, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type, Authorization',
      'Access-Control-Max-Age': '86400'
    });
    res.end();
    return;
  }

  const parsedUrl = url.parse(req.url, true);
  const pathname = parsedUrl.pathname;

  try {
    // 2. Endpoint Upload ảnh lên MinIO S3: POST /api/upload
    if (req.method === 'POST' && pathname === '/api/upload') {
      const payload = await getRequestBody(req);
      const rawData = payload.image || payload.base64 || payload.data;
      if (!rawData || typeof rawData !== 'string') {
        return sendJson(res, 400, { error: 'Tham số `image` (chuỗi base64) là bắt buộc.' });
      }

      let mimeType = 'image/png';
      let cleanBase64 = rawData;
      const mimeMatch = rawData.match(/^data:([a-zA-Z0-9]+\/[a-zA-Z0-9\-\+\.]+);base64,(.+)$/);
      if (mimeMatch) {
        mimeType = mimeMatch[1].toLowerCase();
        cleanBase64 = mimeMatch[2];
      } else if (payload.mimeType) {
        mimeType = String(payload.mimeType).toLowerCase();
      }

      let ext = 'png';
      if (mimeType.indexOf('jpeg') !== -1 || mimeType.indexOf('jpg') !== -1) ext = 'jpg';
      else if (mimeType.indexOf('webp') !== -1) ext = 'webp';
      else if (mimeType.indexOf('gif') !== -1) ext = 'gif';
      else if (mimeType.indexOf('svg') !== -1) ext = 'svg';

      const origName = (payload.name && typeof payload.name === 'string')
        ? payload.name.replace(/[^a-zA-Z0-9_\-\.]/g, '_').replace(/\.[^.]+$/, '')
        : '';
      const uniqueSuffix = Date.now() + '_' + Math.random().toString(36).substring(2, 7);
      const filename = (origName ? (origName.slice(0, 30) + '_') : 'img_') + uniqueSuffix + '.' + ext;

      try {
        const buffer = Buffer.from(cleanBase64, 'base64');
        const uploadResult = await uploadToMinio(filename, buffer, mimeType);
        console.log('[MinIO Upload]: Đã lưu ảnh key=' + filename + ' (' + buffer.length + ' bytes) URL=' + uploadResult.url);
        return sendJson(res, 200, {
          success: true,
          key: filename,
          url: uploadResult.url,
          mimeType: mimeType,
          size: buffer.length,
          base64: 'data:' + mimeType + ';base64,' + cleanBase64
        });
      } catch (uploadErr) {
        console.error('[MinIO Upload Error]:', uploadErr.message);
        return sendJson(res, 502, { error: 'Không thể tải ảnh lên MinIO: ' + uploadErr.message });
      }
    }

    // 3. Endpoint Xóa ảnh khỏi MinIO S3: DELETE /api/upload hoặc POST /api/upload/delete
    if ((req.method === 'DELETE' && pathname === '/api/upload') ||
        (req.method === 'POST' && (pathname === '/api/upload/delete' || pathname === '/api/delete'))) {
      let key = parsedUrl.query.key;
      if (!key) {
        const payload = await getRequestBody(req).catch(function () { return {}; });
        key = payload.key || payload.url;
      }

      if (!key) {
        return sendJson(res, 400, { error: 'Tham số `key` cần xóa là bắt buộc.' });
      }

      try {
        const delResult = await deleteFromMinio(key);
        console.log('[MinIO Delete]: Đã xóa ảnh key=' + delResult.key);
        return sendJson(res, 200, { success: true, key: delResult.key });
      } catch (delErr) {
        console.error('[MinIO Delete Error]:', delErr.message);
        return sendJson(res, 502, { error: 'Không thể xóa ảnh từ MinIO: ' + delErr.message });
      }
    }

    // 4. Endpoint Chat Proxy Streaming: POST /api/chat
    if (req.method === 'POST' && pathname === '/api/chat') {
      const payload = await getRequestBody(req);
      const messages = payload.messages || [];

      if (!Array.isArray(messages) || messages.length === 0) {
        return sendJson(res, 400, { error: 'Tham số `messages` là bắt buộc và phải là mảng không rỗng.' });
      }

      // Xử lý inject knowledgeBase từ frontend (nếu có)
      const knowledgeBase = (payload.knowledgeBase && typeof payload.knowledgeBase === 'string')
        ? payload.knowledgeBase.trim()
        : '';
      const kbPrompt = knowledgeBase ? ('\n\n[Cơ sở tri thức (Knowledge Base)]:\n' + knowledgeBase) : '';

      const thinking = !!payload.thinking;

      const sysMsg = messages.find(function (m) { return m.role === 'system'; });
      if (sysMsg) {
        sysMsg.content = sysMsg.content + kbPrompt + '\n' + SUGGESTIONS_SYSTEM_PROMPT;
      } else {
        messages.unshift({
          role: 'system',
          content: 'Bạn là trợ lý AI thông minh.' + kbPrompt + '\n' + SUGGESTIONS_SYSTEM_PROMPT
        });
      }

      const model = (payload.model && typeof payload.model === 'string' && payload.model.trim())
        ? payload.model.trim()
        : DEFAULT_MODEL;

      console.log('[Chat Request]: POST /api/chat | Messages: ' + messages.length + ' | Model: ' + model + ' | Thinking: ' + thinking);

      // Chuẩn bị payload gửi đến AI API upstream (không gửi tools để tránh model chỉ gọi tool mà bỏ qua sinh nội dung trả lời)
      const requestPayload = {
        model: model,
        messages: messages,
        temperature: FIXED_TEMPERATURE,
        max_tokens: FIXED_MAX_TOKENS,
        max_completion_tokens: FIXED_MAX_TOKENS,
        top_p: FIXED_TOP_P,
        stream: true
      };

      if (thinking) {
        requestPayload.reasoning_format = 'parsed';
      } else {
        requestPayload.reasoning_format = 'hidden';
      }

      const requestBody = JSON.stringify(requestPayload);

      const targetUrl = new url.URL(OPENAI_API_URL);
      const isHttps = targetUrl.protocol === 'https:';
      const client = isHttps ? https : http;

      const reqHeaders = {
        'Content-Type': 'application/json',
        'Content-Length': Buffer.byteLength(requestBody)
      };
      if (OPENAI_API_KEY) {
        reqHeaders['Authorization'] = 'Bearer ' + OPENAI_API_KEY;
      }

      const reqOptions = {
        protocol: targetUrl.protocol,
        hostname: targetUrl.hostname,
        port: targetUrl.port || (isHttps ? 443 : 80),
        path: targetUrl.pathname + (targetUrl.search || ''),
        method: 'POST',
        headers: reqHeaders,
        rejectUnauthorized: REJECT_UNAUTHORIZED
      };

      const proxyReq = client.request(reqOptions, function (apiRes) {
        const statusCode = apiRes.statusCode || 500;

        // Nếu upstream trả mã lỗi HTTP
        if (statusCode < 200 || statusCode >= 300) {
          let errBody = '';
          apiRes.on('data', function (chunk) { errBody += chunk; });
          apiRes.on('end', function () {
            let errMsg = 'Lỗi API (' + statusCode + ')';
            try {
              const errJson = JSON.parse(errBody);
              if (errJson.error && errJson.error.message) {
                errMsg = errJson.error.message;
              } else if (typeof errJson.error === 'string') {
                errMsg = errJson.error;
              }
            } catch (e) { }
            console.error('[Upstream API Error]:', statusCode, errMsg);
            sendJson(res, statusCode, { error: errMsg });
          });
          return;
        }

        // Bắt đầu SSE stream về client
        res.writeHead(200, {
          'Content-Type': 'text/event-stream; charset=utf-8',
          'Cache-Control': 'no-cache, no-transform',
          'Connection': 'keep-alive',
          'Access-Control-Allow-Origin': '*',
          'X-Accel-Buffering': 'no',
          'Transfer-Encoding': 'chunked'
        });

        const decoder = new StringDecoder('utf8');
        let buffer = '';
        const toolCallAccumulator = {};
        let streamEnded = false;
        let lastFinishReason = null;
        let hasLoggedReasoning = false;
        let hasSentAnyContent = false;

        let streamContentBuffer = '';
        let inSuggestions = false;
        let suggestionsRawBuffer = '';

        // Heartbeat giữ kết nối alive khi model đang xử lý suy nghĩ (reasoning/thinking)
        const heartbeatInterval = setInterval(function () {
          if (!res.writableEnded && !streamEnded) {
            try {
              res.write(': heartbeat\n\n');
            } catch (e) { }
          }
        }, HEARTBEAT_INTERVAL_MS);

        function cleanup() {
          if (!streamEnded) {
            streamEnded = true;
            clearInterval(heartbeatInterval);
          }
        }

        // Xử lý stream delta content: tách lọc khối <<<SUGGESTIONS>>> không để lộ JSON ra màn hình
        function handleContentDelta(contentDelta) {
          if (inSuggestions) {
            suggestionsRawBuffer += contentDelta;
            return;
          }

          streamContentBuffer += contentDelta;
          const tagIdx = streamContentBuffer.indexOf(SUGGESTIONS_TAG_START);

          if (tagIdx !== -1) {
            const normalPart = streamContentBuffer.slice(0, tagIdx);
            if (normalPart) {
              hasSentAnyContent = true;
              res.write('data: ' + JSON.stringify({ choices: [{ delta: { content: normalPart } }] }) + '\n\n');
            }
            inSuggestions = true;
            suggestionsRawBuffer = streamContentBuffer.slice(tagIdx + SUGGESTIONS_TAG_START.length);
            streamContentBuffer = '';
          } else {
            // Giữ lại phần đuôi có thể là tiền tố của SUGGESTIONS_TAG_START (tối đa 16 ký tự)
            let matchLen = 0;
            const maxCheck = Math.min(streamContentBuffer.length, SUGGESTIONS_TAG_START.length - 1);
            for (let len = maxCheck; len >= 1; len--) {
              const suffix = streamContentBuffer.slice(-len);
              if (SUGGESTIONS_TAG_START.startsWith(suffix)) {
                matchLen = len;
                break;
              }
            }
            const safeLength = streamContentBuffer.length - matchLen;
            if (safeLength > 0) {
              const toSend = streamContentBuffer.slice(0, safeLength);
              hasSentAnyContent = true;
              res.write('data: ' + JSON.stringify({ choices: [{ delta: { content: toSend } }] }) + '\n\n');
              streamContentBuffer = streamContentBuffer.slice(safeLength);
            }
          }
        }

        // Xử lý từng dòng SSE data
        function processLine(rawLine) {
          const trimmed = rawLine.trim();
          if (!trimmed) return;

          // Bỏ qua comment SSE từ upstream (: OPENROUTER PROCESSING, v.v.)
          if (trimmed.indexOf(':') === 0) return;
          if (trimmed.indexOf('data:') !== 0) return;

          const dataStr = trimmed.replace(/^data:\s*/, '');
          if (dataStr === '[DONE]') return;

          try {
            const parsed = JSON.parse(dataStr);
            const choice = parsed.choices && parsed.choices[0];
            if (!choice) return;

            if (choice.finish_reason) {
              lastFinishReason = choice.finish_reason;
            }

            if (!choice.delta) return;
            const delta = choice.delta;

            // 1. Thu thập tool_call chunks (dự phòng)
            if (delta.tool_calls && Array.isArray(delta.tool_calls)) {
              for (let j = 0; j < delta.tool_calls.length; j++) {
                const tc = delta.tool_calls[j];
                const idx = tc.index !== undefined ? tc.index : 0;
                if (!toolCallAccumulator[idx]) {
                  toolCallAccumulator[idx] = { name: '', argumentsRaw: '' };
                }
                if (tc.function && tc.function.name) {
                  toolCallAccumulator[idx].name += tc.function.name;
                }
                if (tc.function && tc.function.arguments) {
                  toolCallAccumulator[idx].argumentsRaw += tc.function.arguments;
                }
              }
            }

            // 2. Forward delta reasoning (suy nghĩ)
            const reasoningVal = (delta.reasoning !== undefined && delta.reasoning !== null && delta.reasoning !== '')
              ? delta.reasoning
              : ((delta.reasoning_content !== undefined && delta.reasoning_content !== null && delta.reasoning_content !== '')
                ? delta.reasoning_content
                : '');

            if (reasoningVal) {
              if (!hasLoggedReasoning) {
                hasLoggedReasoning = true;
                console.log('[Thinking]: LLM đang truyền reasoning/thinking tokens về client...');
              }
              res.write('data: ' + JSON.stringify({ choices: [{ delta: { reasoning: reasoningVal } }] }) + '\n\n');
            }

            // 3. Xử lý delta text content
            if (delta.content !== undefined && delta.content !== null && delta.content !== '') {
              handleContentDelta(delta.content);
            }
          } catch (e) {
            // Forward raw line nếu không parse được JSON nhưng hợp lệ
            res.write(rawLine + '\n');
          }
        }

        apiRes.on('data', function (chunk) {
          if (streamEnded) return;
          buffer += decoder.write(chunk);
          const lines = buffer.split('\n');
          buffer = lines.pop(); // Giữ lại phần chưa hoàn chỉnh cuối dòng

          for (let i = 0; i < lines.length; i++) {
            processLine(lines[i]);
          }
        });

        apiRes.on('end', function () {
          if (streamEnded) return;
          cleanup();

          // Xử lý phần dữ liệu còn lại trong buffer nếu có
          buffer += decoder.end();
          if (buffer) {
            const remainingLines = buffer.split('\n');
            for (let i = 0; i < remainingLines.length; i++) {
              processLine(remainingLines[i]);
            }
          }

          // Xả nốt buffer nội dung nếu còn sót và chưa vào suggestions mode
          if (streamContentBuffer && !inSuggestions) {
            hasSentAnyContent = true;
            res.write('data: ' + JSON.stringify({ choices: [{ delta: { content: streamContentBuffer } }] }) + '\n\n');
            streamContentBuffer = '';
          }

          let foundSuggestions = false;

          // 1. Phân tích suggestions từ tag <<<SUGGESTIONS>>> trong stream
          if (suggestionsRawBuffer) {
            try {
              let cleanJson = suggestionsRawBuffer;
              const endIdx = cleanJson.indexOf(SUGGESTIONS_TAG_END);
              if (endIdx !== -1) {
                cleanJson = cleanJson.slice(0, endIdx);
              }
              cleanJson = cleanJson.replace(/^```(?:json)?\s*|```$/g, '').trim();

              let title = '';
              let questions = [];

              try {
                const parsedArgs = JSON.parse(cleanJson);
                if (typeof parsedArgs.title === 'string' && parsedArgs.title.trim()) {
                  title = parsedArgs.title.trim();
                }
                if (Array.isArray(parsedArgs.questions)) {
                  questions = parsedArgs.questions;
                }
              } catch (parseErr) {
                const titleMatch = cleanJson.match(/"title"\s*:\s*"([^"\\]*(?:\\.[^"\\]*)*)"/);
                if (titleMatch && titleMatch[1]) {
                  try { title = JSON.parse('"' + titleMatch[1] + '"'); } catch (e) { title = titleMatch[1]; }
                }
                const qMatch = cleanJson.match(/"questions"\s*:\s*\[([\s\S]*?)\]/);
                if (qMatch && qMatch[1]) {
                  const items = qMatch[1].match(/"([^"\\]*(?:\\.[^"\\]*)*)"/g);
                  if (items) {
                    questions = items.map(function (item) {
                      try { return JSON.parse(item); } catch (e) { return item.replace(/^"|"$/g, ''); }
                    });
                  }
                }
              }

              questions = questions
                .slice(0, 3)
                .map(function (q) { return String(q).trim(); })
                .filter(Boolean);

              if (questions.length > 0) {
                foundSuggestions = true;
                console.log('[Tag Suggestions] Tiêu đề:', title || '(Mặc định)', '| Gợi ý:', questions);
                res.write('data: ' + JSON.stringify({
                  __suggestions__: questions,
                  __suggestions_title__: title
                }) + '\n\n');
              }
            } catch (e) {
              console.error('[Tag Suggestions Parse Error]:', e.message);
            }
          }

          // 2. Dự phòng: Phân tích nếu model gọi tool suggest_followup_questions
          if (!foundSuggestions) {
            try {
              const keys = Object.keys(toolCallAccumulator);
              for (let k = 0; k < keys.length; k++) {
                const tc = toolCallAccumulator[keys[k]];
                const isSuggestTool = tc.name && tc.name.indexOf('suggest_followup_questions') !== -1;
                if (isSuggestTool && tc.argumentsRaw) {
                  let questions = [];
                  let title = '';
                  try {
                    const rawClean = tc.argumentsRaw.replace(/^```json\s*|```$/g, '').trim();
                    const args = JSON.parse(rawClean);
                    if (typeof args.title === 'string' && args.title.trim()) {
                      title = args.title.trim();
                    }
                    if (Array.isArray(args.questions)) {
                      questions = args.questions;
                    }
                  } catch (parseErr) {
                    const matchTitle = tc.argumentsRaw.match(/"title"\s*:\s*"([^"\\]*(?:\\.[^"\\]*)*)"/);
                    if (matchTitle && matchTitle[1]) {
                      try { title = JSON.parse('"' + matchTitle[1] + '"'); } catch (e) { title = matchTitle[1]; }
                    }
                    const match = tc.argumentsRaw.match(/"questions"\s*:\s*\[([\s\S]*?)\]/);
                    if (match && match[1]) {
                      const items = match[1].match(/"([^"\\]*(?:\\.[^"\\]*)*)"/g);
                      if (items) {
                        questions = items.map(function (item) {
                          try { return JSON.parse(item); } catch (e) { return item.replace(/^"|"$/g, ''); }
                        });
                      }
                    }
                  }

                  questions = questions
                    .slice(0, 3)
                    .map(function (q) { return String(q).trim(); })
                    .filter(Boolean);

                  if (questions.length > 0) {
                    foundSuggestions = true;
                    console.log('[Tool Call Suggest] Tiêu đề:', title || '(Không có)', '| Gợi ý:', questions);
                    res.write('data: ' + JSON.stringify({
                      __suggestions__: questions,
                      __suggestions_title__: title
                    }) + '\n\n');
                  }
                }
              }
            } catch (e) {
              console.error('[Suggest Tool Parse Error]:', e.message);
            }
          }

          // Bảo vệ: Nếu LLM hoàn toàn không trả về nội dung nào
          if (!hasSentAnyContent) {
            console.warn('[Warning]: Upstream không trả về content nào.');
            res.write('data: ' + JSON.stringify({ choices: [{ delta: { content: 'Xin lỗi, không nhận được phản hồi từ mô hình AI. Vui lòng thử lại.' } }] }) + '\n\n');
          }

          if (lastFinishReason === 'length') {
            console.warn('⚠️ [Token Limit Warning]: Câu trả lời bị cắt giữa chừng do chạm giới hạn token (finish_reason: "length")! Hãy tăng AI_MAX_TOKENS trong .env nếu muốn sinh nội dung dài hơn.');
          } else if (lastFinishReason) {
            console.log('[Stream]: Hoàn tất với finish_reason = ' + lastFinishReason);
          }

          res.write('data: [DONE]\n\n');
          res.end();
        });

        apiRes.on('error', function (err) {
          cleanup();
          console.error('[Upstream Stream Error]:', err.message);
          if (!res.headersSent) {
            sendJson(res, 502, { error: 'Lỗi stream từ AI upstream: ' + err.message });
          } else {
            res.write('data: ' + JSON.stringify({ error: err.message }) + '\n\n');
            res.write('data: [DONE]\n\n');
            res.end();
          }
        });
      });

      proxyReq.on('error', function (err) {
        console.error('[Upstream Request Error]:', err.message);
        if (!res.headersSent) {
          sendJson(res, 502, { error: 'Không thể kết nối đến AI API: ' + (err.message || 'Lỗi mạng') });
        } else {
          res.end();
        }
      });

      // Nếu client ngắt kết nối giữa chừng (user hủy hoặc đóng tab)
      req.on('close', function () {
        if (!proxyReq.finished) {
          if (proxyReq.destroy) {
            proxyReq.destroy();
          } else if (proxyReq.abort) {
            proxyReq.abort();
          }
        }
      });

      proxyReq.write(requestBody);
      proxyReq.end();
      return;
    }

    // Không tìm thấy route phù hợp
    return sendJson(res, 404, { error: 'Route không tồn tại' });
  } catch (err) {
    console.error('[Server API Error]:', err);
    return sendJson(res, 500, { error: err.message || 'Lỗi xử lý máy chủ' });
  }
});

server.listen(PORT, '0.0.0.0', function () {
  console.log('====================================================');
  console.log('🚀 UrChatbot API Service (OpenAI-compatible)');
  console.log('📌 Node.js version  : ' + process.version);
  console.log('📌 Port             : ' + PORT);
  console.log('📌 API Endpoint     : http://localhost:' + PORT + '/api/chat');
  console.log('📌 Upstream URL     : ' + OPENAI_API_URL);
  console.log('📌 Default Model    : ' + DEFAULT_MODEL);
  console.log('📌 API Key          : ' + maskKey(OPENAI_API_KEY));
  console.log('📌 SSL Verify       : ' + (REJECT_UNAUTHORIZED ? 'Bật (Strict)' : 'Tắt (Bypass cert chain)'));
  console.log('====================================================');
});
