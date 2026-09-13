package com.chatbot.entity;

public enum ToolType {
    STATIC_MARKDOWN, // Trả về nội dung Markdown / Cache (vd: thực đơn Canteen)
    SQL_QUERY,       // Chạy truy vấn SQL trên H2 Database (vd: thời tiết)
    QDRANT_VECTOR,   // Tìm kiếm ngữ nghĩa vector (vd: tài liệu sản xuất / pháp luật)
    HTTP_API         // Gọi HTTP REST API bên ngoài
}
