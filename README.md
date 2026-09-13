# 🚀 ASTRO AI STUDIO & ASSISTANT CHAT

Hệ sinh thái ứng dụng Trợ lý AI toàn diện (AI Assistant & Studio Quản Trị Tri Thức RAG) được xây dựng trên nền tảng **Vue.js**, **Spring Boot 2.1.18**, **H2 Database (file-based)** và **MinIO S3 Compatible Storage**.

Hỗ trợ lưu trữ đa phiên hội thoại bền vững vào cơ sở dữ liệu H2 DB phân vùng theo người dùng, định tuyến session theo short UUID, tự động chuyển hướng thông minh, vẽ sơ đồ Mermaid, trích xuất code block với gutter và nút copy tiện lợi.

---

## 📑 MỤC LỤC
1. [Kiến Trúc & Công Nghệ](#-kiến-trúc--công-nghệ)
2. [Cơ Sở Dữ Liệu & Cấu Trúc Bảng](#-cơ-sở-dữ-liệu--cấu-trúc-bảng)
   - [Bảng chat_sessions](#1-bảng-chat_sessions-lưu-phiên-trò-chuyện)
   - [Bảng chat_messages](#2-bảng-chat_messages-lưu-nội-dung-tin-nhắn)
   - [Bảng dynamic_tools & RAG](#3-bảng-dynamic_tools--rag-knowledge-bases)
3. [Script Tạo Bảng (SQL DDL)](#-script-tạo-bảng-sql-ddl)
4. [Hướng Dẫn Cài Đặt & Khởi Chạy](#-hướng-dẫn-cài-đặt--khởi-chạy)
5. [Thông Tin Người Dùng & Định Tuyến](#-thông-tin-người-dùng--định-tuyến-url)

---

## 🛠 KIẾN TRÚC & CÔNG NGHỆ

- **Frontend**: Vue.js 2.6, Vue Router, Webpack, Vanilla SCSS/CSS Module, Highlight.js, Mermaid.js.
- **Backend**: Java 17+, Spring Boot 2.1.18, Spring Data JPA / Hibernate, SSE (Server-Sent Events) streaming.
- **Database**: H2 Database Engine (chế độ file-based tại `./data/chatbotdb`).
  - H2 Web Console: `http://127.0.0.1:3001/h2-console` (JDBC URL: `jdbc:h2:file:./data/chatbotdb`, User: `sa`, Pass: để trống).
- **Storage**: MinIO S3 Server cục bộ quản lý ảnh đính kèm và tài liệu.
- **AI Models**: Tương thích chuẩn OpenAI API (hỗ trợ Groq, OpenAI, Ollama, OpenRouter...) với các model `openai/gpt-oss-120b`, `qwen/qwen3.8-27b` (Vision), `llama-3.3-70b-versatile`.

---

## 🗄 CƠ SỞ DỮ LIỆU & CẤU TRÚC BẢNG

Toàn bộ phiên trò chuyện và tin nhắn được lưu trữ trong 2 bảng liên kết quan hệ trong H2 DB:

### 1. Bảng `chat_sessions` (Lưu Phiên Trò Chuyện)
Lưu trữ danh mục các cuộc trò chuyện của từng người dùng.

| Tên Cột | Kiểu Dữ Liệu | Khóa / Ràng Buộc | Mô Tả |
| :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(64)` | **PRIMARY KEY** | UUID dạng ngắn (12 ký tự hex, ví dụ `44c627a93d22`), không chứa `-` hay `_` |
| `user_id` | `VARCHAR(64)` | `NOT NULL, INDEX` | ID định danh người dùng sở hữu phiên (mặc định: `nam.dovan`) |
| `user_name` | `VARCHAR(128)` | Nullable | Tên hiển thị người dùng (mặc định: `Do Van Nam`) |
| `title` | `VARCHAR(255)` | `NOT NULL` | Tiêu đề cuộc hội thoại (tự sinh thông minh hoặc do user đổi) |
| `emoji` | `VARCHAR(16)` | Default `'💬'` | Biểu tượng cảm xúc đại diện cho ngữ cảnh phiên chat |
| `is_custom_renamed` | `BOOLEAN` | Default `FALSE` | Đánh dấu phiên đã được người dùng chủ động đổi tên thủ công |
| `model` | `VARCHAR(64)` | Nullable | Model AI đã chọn cho phiên trò chuyện |
| `created_at` | `BIGINT` | `NOT NULL` | Unix timestamp thời điểm tạo phiên (mili-giây) |
| `updated_at` | `BIGINT` | `NOT NULL, INDEX` | Unix timestamp cập nhật tin nhắn cuối cùng (sắp xếp giảm dần) |

---

### 2. Bảng `chat_messages` (Lưu Nội Dung Tin Nhắn)
Lưu trữ chi tiết từng tin nhắn (User, Bot Assistant, System) thuộc về từng phiên chat.

| Tên Cột | Kiểu Dữ Liệu | Khóa / Ràng Buộc | Mô Tả |
| :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | **PRIMARY KEY (AUTO_INC)** | ID tự tăng của bản ghi tin nhắn |
| `session_id` | `VARCHAR(64)` | `NOT NULL, FK, INDEX` | Khóa ngoại liên kết tới `chat_sessions(id)` (ON DELETE CASCADE) |
| `user_id` | `VARCHAR(64)` | `NOT NULL` | ID người dùng gửi / nhận tin nhắn |
| `role` | `VARCHAR(32)` | `NOT NULL` | Vai trò: `user`, `assistant`, `bot`, `system` |
| `content` | `CLOB` | Nullable | Nội dung văn bản Markdown thô của tin nhắn |
| `thinking` | `CLOB` | Nullable | Nội dung suy luận lý luận Reasoning (Chain-of-Thought) của model |
| `html_content` | `CLOB` | Nullable | Nội dung HTML đã render sẵn (tăng tốc độ nạp lịch sử 0ms) |
| `images_json` | `CLOB` | Nullable | Danh sách ảnh đính kèm (JSON Array: url, base64, tên file...) |
| `versions_json` | `CLOB` | Nullable | Lịch sử các phiên bản tin nhắn (khi sửa câu hỏi hoặc bấm regenerate) |
| `order_idx` | `INT` | Default `0` | Số thứ tự sắp xếp hiển thị tin nhắn trong phiên chat |
| `created_at` | `BIGINT` | `NOT NULL` | Unix timestamp thời điểm tin nhắn được gửi (mili-giây) |

---

### 3. Bảng `dynamic_tools` & RAG Knowledge Bases
Dành cho Agent Function Calling và Studio Quản trị Tri thức RAG:
- `dynamic_tools`: Lưu danh mục công cụ động (Markdown, SQL, API Endpoint) cho AI tự động kích hoạt.
- `knowledge_bases`: Nhóm kho tài liệu chuyên ngành.
- `knowledge_documents`: Nội dung tài liệu toàn văn tải lên kho.
- `knowledge_chunks`: Các đoạn văn bản đã được cắt nhỏ (chunking) phục vụ tìm kiếm ngữ nghĩa và trích xuất ngữ cảnh.

---

## 📜 SCRIPT TẠO BẢNG (SQL DDL)

Bạn có thể chạy script sau trên H2 Console (`http://127.0.0.1:3001/h2-console`) hoặc bất kỳ RDBMS tương thích nào (MySQL, PostgreSQL, v.v.):

```sql
-- 1. BẢNG PHIÊN TRÒ CHUYỆN (CHAT SESSIONS)
CREATE TABLE IF NOT EXISTS chat_sessions (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    user_name VARCHAR(128),
    title VARCHAR(255) NOT NULL,
    emoji VARCHAR(16) DEFAULT '💬',
    is_custom_renamed BOOLEAN DEFAULT FALSE,
    model VARCHAR(64) DEFAULT '',
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_chat_sessions_user ON chat_sessions (user_id);
CREATE INDEX IF NOT EXISTS idx_chat_sessions_updated ON chat_sessions (updated_at DESC);

-- 2. BẢNG NỘI DUNG TIN NHẮN (CHAT MESSAGES)
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    role VARCHAR(32) NOT NULL,
    content CLOB,
    thinking CLOB,
    html_content CLOB,
    images_json CLOB,
    versions_json CLOB,
    order_idx INT DEFAULT 0,
    created_at BIGINT NOT NULL,
    CONSTRAINT fk_chat_messages_session FOREIGN KEY (session_id) 
        REFERENCES chat_sessions(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_chat_messages_session ON chat_messages (session_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_user_sess ON chat_messages (session_id, user_id, order_idx ASC);

-- 3. BẢNG DYNAMIC TOOLS (AGENT TOOLS)
CREATE TABLE IF NOT EXISTS dynamic_tools (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500) NOT NULL,
    tool_type VARCHAR(50) NOT NULL,
    parameters_schema CLOB NOT NULL,
    config_data CLOB NOT NULL,
    enabled BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. BẢNG KNOWLEDGE BASES (RAG)
CREATE TABLE IF NOT EXISTS knowledge_bases (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    icon VARCHAR(50) DEFAULT '📚',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. BẢNG KNOWLEDGE DOCUMENTS
CREATE TABLE IF NOT EXISTS knowledge_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    knowledge_base_id BIGINT NOT NULL,
    title VARCHAR(250) NOT NULL,
    source_type VARCHAR(50) DEFAULT 'TEXT',
    content CLOB NOT NULL,
    char_count INT DEFAULT 0,
    chunk_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_knowledge_docs_kb FOREIGN KEY (knowledge_base_id) 
        REFERENCES knowledge_bases(id) ON DELETE CASCADE
);

-- 6. BẢNG KNOWLEDGE CHUNKS
CREATE TABLE IF NOT EXISTS knowledge_chunks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id BIGINT NOT NULL,
    knowledge_base_id BIGINT NOT NULL,
    chunk_index INT NOT NULL,
    content CLOB NOT NULL,
    char_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_knowledge_chunks_doc FOREIGN KEY (document_id) 
        REFERENCES knowledge_documents(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_chunk_kb ON knowledge_chunks (knowledge_base_id);
CREATE INDEX IF NOT EXISTS idx_chunk_doc ON knowledge_chunks (document_id);
```

*File script độc lập cũng được đặt tại:* [database/schema_h2.sql](database/schema_h2.sql) và [backend_java/src/main/resources/schema.sql](backend_java/src/main/resources/schema.sql).

---

## ⚡ HƯỚNG DẪN CÀI ĐẶT & KHỞI CHẠY

### 1. Khởi động MinIO S3 (Tùy chọn)
Chạy tệp batch đã có sẵn trong thư mục `minio/`:
```bash
cd minio
start_minio.bat
```
*(Endpoint mặc định: `http://127.0.0.1:9000`, Console: `http://127.0.0.1:9001`)*

### 2. Khởi động Backend Spring Boot (Cổng 3001)
Yêu cầu Java 17+ và Maven:
```bash
cd backend_java
mvn spring-boot:run
```
Backend tự động tạo cấu trúc bảng trên H2 Database (`./data/chatbotdb`) và cung cấp các REST API:
- `GET /api/sessions?userId=nam.dovan`: Lấy danh sách session
- `GET /api/sessions/{id}?userId=nam.dovan`: Lấy chi tiết phiên và lịch sử chat
- `POST /api/sessions`: Lưu/cập nhật phiên và tin nhắn
- `POST /api/chat`: Xử lý streaming AI completions (SSE)
- `POST /api/chat/title`: Sinh tiêu đề thông minh tự động

### 3. Khởi động Frontend Vue.js (Cổng 3002)
Tại thư mục gốc dự án:
```bash
npm install
npm run dev
```
Truy cập trình duyệt tại: **`http://127.0.0.1:3002/`**

---

## 👤 THÔNG TIN NGƯỜI DÙNG & ĐỊNH TUYẾN URL

### 1. Người Dùng Giả Lập Mặc Định
- **User ID**: `nam.dovan`
- **User Name**: `Do Van Nam`
- Toàn bộ session và message khi lưu trữ hoặc truy vấn đều được gắn với `userId` này để bảo đảm tính riêng tư và phân vùng dữ liệu rõ ràng.

### 2. Định Tuyến URL Thông Minh
- **Giao diện Chat Chính**: `http://127.0.0.1:3002/chat`
- **Truy cập trực tiếp phiên chat**: `http://127.0.0.1:3002/chat/{uuid}`
  - Ví dụ: `http://127.0.0.1:3002/chat/44c627a93d22`
  - Bấm F5 hoặc gửi đường link cho người khác trên cùng môi trường sẽ tự động nạp lại toàn bộ phiên và tin nhắn từ H2 DB.
- **Tự động Chuyển Hướng khi ID Không Tồn Tại**:
  - Khi mở đường link có session ID không hợp lệ (ví dụ: `/chat/44c627a93123`), hệ thống tự động redirect về `/chat` với một phiên trò chuyện mới tinh, sẵn sàng nhận câu hỏi.
  - Khi người dùng gửi câu hỏi đầu tiên ở phiên mới, URL sẽ tự động cập nhật sang ID mới mà không reload trang.
