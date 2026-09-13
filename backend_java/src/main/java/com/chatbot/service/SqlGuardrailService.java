package com.chatbot.service;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class SqlGuardrailService {

    private static final Pattern DANGEROUS_KEYWORDS = Pattern.compile(
            "\\b(DROP|DELETE|UPDATE|INSERT|ALTER|TRUNCATE|EXEC|EXECUTE|CREATE|GRANT|REVOKE|MERGE|REPLACE)\\b",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Kiểm tra tính an toàn của câu lệnh SQL trước khi thực thi
     */
    public String validateAndSanitize(String rawSql) {
        if (rawSql == null || rawSql.trim().isEmpty()) {
            throw new IllegalArgumentException("Câu lệnh SQL không được để trống.");
        }

        String sql = rawSql.trim();

        // 1. Chỉ cho phép bắt đầu bằng SELECT
        if (!sql.toUpperCase().startsWith("SELECT")) {
            throw new SecurityException("Chính sách bảo mật: Chỉ cho phép thực thi câu lệnh truy vấn đọc dữ liệu (SELECT).");
        }

        // 2. Chặn các từ khóa phá hoại / thay đổi dữ liệu
        if (DANGEROUS_KEYWORDS.matcher(sql).find()) {
            throw new SecurityException("Chính sách bảo mật: Câu lệnh SQL chứa từ khóa bị cấm (chỉ cho phép truy vấn SELECT).");
        }

        // 3. Chặn tấn công đa truy vấn (multi-statement) bằng dấu chấm phẩy
        if (sql.contains(";")) {
            String[] parts = sql.split(";");
            if (parts.length > 1 && !parts[1].trim().isEmpty()) {
                throw new SecurityException("Chính sách bảo mật: Không cho phép thực thi nhiều câu lệnh SQL đồng thời.");
            }
            sql = parts[0].trim();
        }

        // 4. Giới hạn số lượng bản ghi trả về để bảo vệ tài nguyên hệ thống (tối đa 50)
        if (!sql.toUpperCase().contains("LIMIT")) {
            sql = sql + " LIMIT 50";
        }

        return sql;
    }
}
