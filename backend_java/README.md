# Chatbot Backend Service - Spring Boot 2.1.18 (Java 8)

Dự án backend Spring Boot 2.1.18 (Java 8, Maven) được chuyển đổi hoàn chỉnh từ `server.js` của **UrChatbot Widget**.

---

## 🚀 Tính năng

1. **Streaming Chat SSE (`POST /api/chat`)**:
   - Tương thích chuẩn OpenAI Streaming Chat Completions API (Groq, OpenRouter, OpenAI, v.v.).
   - Hỗ trợ **Server-Sent Events (SSE)** với kết nối liên tục, độ trễ cực thấp.
   - Hỗ trợ **Multimodal Vision** (gửi mảng text + image_url base64).
   - Hỗ trợ **Reasoning / Thinking tokens** (`reasoning_format: parsed / hidden`).
   - Tự động lọc và tách khối `<<<SUGGESTIONS>>>` để trả về gợi ý câu hỏi tiếp theo kèm tiêu đề:
     ```json
     data: {"__suggestions__": ["Câu hỏi 1", "Câu hỏi 2"], "__suggestions_title__": "Tiêu đề gợi ý"}
     ```
   - Cơ chế **Heartbeat SSE** định kỳ (`: heartbeat\n\n`) giữ kết nối không bị ngắt khi model xử lý suy nghĩ lâu.

2. **Upload & Quản lý file MinIO S3 (`POST /api/upload`, `DELETE /api/upload`)**:
   - Nhận ảnh base64, giải mã và đẩy trực tiếp lên MinIO bucket.
   - Hỗ trợ các route dự phòng: `POST /api/upload/delete` và `POST /api/delete`.

3. **Cấu hình linh hoạt**:
   - Tự động nạp file `.env` từ thư mục gốc hoặc thư mục hiện tại khi khởi động.
   - Có thể ghi đè qua `application.properties` hoặc biến môi trường hệ thống.

---

## 🛠️ Cấu trúc thư mục

```
backend_java/
├── pom.xml
├── .gitignore
├── README.md
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── chatbot/
        │           ├── ChatbotApplication.java          # Main Application & Auto .env Loader
        │           ├── config/
        │           │   ├── AppProperties.java           # Binding cấu hình chatbot.*
        │           │   └── CorsConfig.java              # Cấu hình CORS mở cho /api/**
        │           ├── controller/
        │           │   ├── ChatController.java          # Controller xử lý POST /api/chat
        │           │   └── UploadController.java        # Controller xử lý Upload & Delete ảnh
        │           ├── dto/
        │           │   ├── ChatRequest.java             # DTO nhận request chat
        │           │   ├── MessageDto.java              # DTO tin nhắn (hỗ trợ text & vision)
        │           │   ├── UploadRequest.java           # DTO nhận base64 upload/delete
        │           │   └── UploadResponse.java          # DTO phản hồi upload/delete
        │           └── service/
        │               ├── AiChatService.java           # Logic gọi Upstream AI, parse tag, SSE stream
        │               └── MinioService.java            # Logic upload/xóa binary trên MinIO S3
        └── resources/
            └── application.properties                   # File cấu hình mặc định
```

---

## 📦 Yêu cầu hệ thống
- **Java**: JDK 8 (hoặc mới hơn, dự án target Java 8 tương thích hoàn toàn).
- **Maven**: 3.6+

---

## ⚡ Hướng dẫn biên dịch & chạy

### 1. Biên dịch dự án
```bash
cd backend_java
mvn clean package -DskipTests
```

### 2. Chạy ứng dụng Spring Boot
```bash
# Cách 1: Chạy bằng Maven plugin
mvn spring-boot:run

# Cách 2: Chạy file JAR đã đóng gói
java -jar target/chatbot-backend-java-1.0.0.jar
```

Dịch vụ sẽ khởi chạy tại cổng **3001** (mặc định khớp với frontend chatbot Vue).
- Endpoint Chat: `http://localhost:3001/api/chat`
- Endpoint Upload: `http://localhost:3001/api/upload`
