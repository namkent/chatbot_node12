package com.chatbot.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/db")
public class DatabaseExplorerController {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseExplorerController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Danh sách các bảng dữ liệu trong H2 Database và số lượng bản ghi
     */
    @GetMapping("/tables")
    public List<Map<String, Object>> listTables() {
        List<String> managedTables = Arrays.asList(
                "DYNAMIC_TOOLS",
                "WEATHER_LOGS",
                "KNOWLEDGE_BASES",
                "KNOWLEDGE_DOCUMENTS",
                "KNOWLEDGE_CHUNKS"
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (String table : managedTables) {
            try {
                Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
                Map<String, Object> map = new HashMap<>();
                map.put("tableName", table.toLowerCase());
                map.put("rowCount", count != null ? count : 0);
                result.add(map);
            } catch (Exception ignored) {}
        }
        return result;
    }

    /**
     * Xem dữ liệu của một bảng cụ thể (tối đa 50 dòng mới nhất)
     */
    @GetMapping("/tables/{tableName}")
    public ResponseEntity<?> getTableData(@PathVariable String tableName) {
        String cleanName = tableName.trim().toUpperCase();
        List<String> allowed = Arrays.asList(
                "DYNAMIC_TOOLS",
                "WEATHER_LOGS",
                "KNOWLEDGE_BASES",
                "KNOWLEDGE_DOCUMENTS",
                "KNOWLEDGE_CHUNKS"
        );

        if (!allowed.contains(cleanName)) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Bảng không được phép truy cập"));
        }

        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM " + cleanName + " ORDER BY id DESC LIMIT 50");

            List<String> columns = new ArrayList<>();
            if (!rows.isEmpty()) {
                columns.addAll(rows.get(0).keySet());
            }

            Map<String, Object> res = new HashMap<>();
            res.put("tableName", cleanName.toLowerCase());
            res.put("totalRows", rows.size());
            res.put("columns", columns);
            res.put("rows", rows);

            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}
