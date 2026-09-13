-- ==============================================================================
-- DATABASE SCHEMA SCRIPT: H2 DATABASE / RDBMS COMPATIBLE
-- Hệ Thống: Astro AI Studio & Assistant Chat
-- Tác giả: Do Van Nam (nam.dovan)
-- ==============================================================================

-- 1. BẢNG PHIÊN TRÒ CHUYỆN (CHAT SESSIONS)
CREATE TABLE IF NOT EXISTS chat_sessions (
    id VARCHAR(64) PRIMARY KEY,                 -- UUID dạng ngắn (12 ký tự hex không chứa '-', '_')
    user_id VARCHAR(64) NOT NULL,               -- ID người dùng (mặc định: 'nam.dovan')
    user_name VARCHAR(128),                     -- Tên hiển thị người dùng ('Do Van Nam')
    title VARCHAR(255) NOT NULL,                -- Tiêu đề phiên trò chuyện
    emoji VARCHAR(16) DEFAULT '💬',             -- Emoji đại diện cho chủ đề cuộc trò chuyện
    is_custom_renamed BOOLEAN DEFAULT FALSE,    -- Đánh dấu người dùng đã tự đổi tên thủ công
    model VARCHAR(64) DEFAULT '',               -- Model AI sử dụng (ví dụ: 'openai/gpt-oss-120b')
    created_at BIGINT NOT NULL,                 -- Thời gian tạo (Unix timestamp ms)
    updated_at BIGINT NOT NULL                  -- Thời gian cập nhật gần nhất (Unix timestamp ms)
);

CREATE INDEX IF NOT EXISTS idx_chat_sessions_user ON chat_sessions (user_id);
CREATE INDEX IF NOT EXISTS idx_chat_sessions_updated ON chat_sessions (updated_at DESC);


-- 2. BẢNG NỘI DUNG TIN NHẮN (CHAT MESSAGES)
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,       -- Khóa chính tự tăng
    session_id VARCHAR(64) NOT NULL,            -- ID phiên trò chuyện (FK liên kết chat_sessions)
    user_id VARCHAR(64) NOT NULL,               -- ID người dùng sở hữu tin nhắn
    role VARCHAR(32) NOT NULL,                  -- Vai trò người gửi: 'user', 'assistant', 'bot', 'system'
    content CLOB,                               -- Nội dung văn bản Markdown gốc của tin nhắn
    thinking CLOB,                              -- Nội dung suy luận lý luận Reasoning (CoT) của mô hình
    html_content CLOB,                          -- Nội dung đã render sẵn sang HTML để nạp tức thì
    images_json CLOB,                           -- Danh sách ảnh đính kèm (JSON Array: name, url/b64, mode)
    versions_json CLOB,                         -- Lịch sử các phiên bản sửa/regenerate (JSON Array)
    order_idx INT DEFAULT 0,                    -- Thứ tự hiển thị tin nhắn trong phiên chat (tăng dần)
    created_at BIGINT NOT NULL,                 -- Thời gian tạo tin nhắn (Unix timestamp ms)
    CONSTRAINT fk_chat_messages_session FOREIGN KEY (session_id) 
        REFERENCES chat_sessions(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_chat_messages_session ON chat_messages (session_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_user_sess ON chat_messages (session_id, user_id, order_idx ASC);


-- 3. BẢNG CÔNG CỤ ĐỘNG CỦA AGENT (DYNAMIC TOOLS)
CREATE TABLE IF NOT EXISTS dynamic_tools (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,          -- Tên công cụ (function name cho LLM)
    description VARCHAR(500) NOT NULL,          -- Mô tả chức năng để LLM tự quyết định gọi
    tool_type VARCHAR(50) NOT NULL,             -- Loại tool: MARKDOWN, DATABASE_SQL, QDRANT_SEARCH, HTTP_API
    parameters_schema CLOB NOT NULL,            -- JSON Schema mô tả tham số đầu vào
    config_data CLOB NOT NULL,                  -- Cấu hình thực thi (SQL, Markdown, Endpoint...)
    enabled BOOLEAN DEFAULT TRUE NOT NULL,      -- Bật / Tắt công cụ
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- 4. BẢNG DANH MỤC KHO TRI THỨC RAG (KNOWLEDGE BASES)
CREATE TABLE IF NOT EXISTS knowledge_bases (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,                 -- Tên kho tri thức
    description TEXT,                           -- Mô tả phạm vi kho tri thức
    icon VARCHAR(50) DEFAULT '📚',              -- Emoji icon đại diện
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- 5. BẢNG TÀI LIỆU NGUỒN TRONG KHO TRI THỨC (KNOWLEDGE DOCUMENTS)
CREATE TABLE IF NOT EXISTS knowledge_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    knowledge_base_id BIGINT NOT NULL,          -- FK tới kho tri thức tương ứng
    title VARCHAR(250) NOT NULL,                -- Tiêu đề tài liệu
    source_type VARCHAR(50) DEFAULT 'TEXT',     -- Loại nguồn: TEXT, MARKDOWN, PDF, FILE
    content CLOB NOT NULL,                      -- Nội dung văn bản toàn văn
    char_count INT DEFAULT 0,                   -- Số lượng ký tự
    chunk_count INT DEFAULT 0,                  -- Số đoạn chunk đã chia nhỏ
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_knowledge_docs_kb FOREIGN KEY (knowledge_base_id) 
        REFERENCES knowledge_bases(id) ON DELETE CASCADE
);


-- 6. BẢNG ĐOẠN VĂN BẢN CHIA NHỎ (KNOWLEDGE CHUNKS)
CREATE TABLE IF NOT EXISTS knowledge_chunks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id BIGINT NOT NULL,                -- FK tới tài liệu nguồn
    knowledge_base_id BIGINT NOT NULL,          -- FK tới kho tri thức
    chunk_index INT NOT NULL,                   -- Thứ tự index của đoạn chunk (0, 1, 2...)
    content CLOB NOT NULL,                      -- Nội dung đoạn cắt nhỏ
    char_count INT DEFAULT 0,                   -- Độ dài đoạn cắt
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_knowledge_chunks_doc FOREIGN KEY (document_id) 
        REFERENCES knowledge_documents(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_chunk_kb ON knowledge_chunks (knowledge_base_id);
CREATE INDEX IF NOT EXISTS idx_chunk_doc ON knowledge_chunks (document_id);
