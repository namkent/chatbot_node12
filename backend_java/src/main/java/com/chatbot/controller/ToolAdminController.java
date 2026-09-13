package com.chatbot.controller;

import com.chatbot.dto.ToolDto;
import com.chatbot.entity.DynamicToolEntity;
import com.chatbot.repository.DynamicToolRepository;
import com.chatbot.service.DynamicToolExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tools")
public class ToolAdminController {

    private final DynamicToolRepository toolRepository;
    private final DynamicToolExecutor toolExecutor;

    public ToolAdminController(DynamicToolRepository toolRepository, DynamicToolExecutor toolExecutor) {
        this.toolRepository = toolRepository;
        this.toolExecutor = toolExecutor;
    }

    /**
     * Lấy danh sách toàn bộ các Tool trong hệ thống
     */
    @GetMapping
    public ResponseEntity<List<ToolDto>> getAllTools() {
        List<ToolDto> list = toolRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    /**
     * Lấy chi tiết 1 Tool theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ToolDto> getToolById(@PathVariable Long id) {
        return toolRepository.findById(id)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Tạo Tool mới
     */
    @PostMapping
    public ResponseEntity<?> createTool(@RequestBody ToolDto dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Tên Tool không được để trống."));
        }
        if (toolRepository.existsByName(dto.getName().trim())) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Tên Tool '" + dto.getName() + "' đã tồn tại trong hệ thống."));
        }

        DynamicToolEntity entity = new DynamicToolEntity();
        updateEntityFromDto(entity, dto);
        DynamicToolEntity saved = toolRepository.save(entity);
        return ResponseEntity.ok(toDto(saved));
    }

    /**
     * Cập nhật Tool theo ID
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTool(@PathVariable Long id, @RequestBody ToolDto dto) {
        Optional<DynamicToolEntity> opt = toolRepository.findById(id);
        if (!opt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        DynamicToolEntity entity = opt.get();
        if (!entity.getName().equals(dto.getName().trim()) && toolRepository.existsByName(dto.getName().trim())) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Tên Tool '" + dto.getName() + "' đã bị trùng lặp."));
        }

        updateEntityFromDto(entity, dto);
        DynamicToolEntity updated = toolRepository.save(entity);
        return ResponseEntity.ok(toDto(updated));
    }

    /**
     * Xóa Tool
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTool(@PathVariable Long id) {
        if (!toolRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        toolRepository.deleteById(id);
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }

    /**
     * Bật/Tắt trạng thái hoạt động của Tool (Toggle switch)
     */
    @PostMapping("/{id}/toggle")
    public ResponseEntity<?> toggleTool(@PathVariable Long id) {
        Optional<DynamicToolEntity> opt = toolRepository.findById(id);
        if (!opt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        DynamicToolEntity entity = opt.get();
        entity.setEnabled(!entity.isEnabled());
        toolRepository.save(entity);
        return ResponseEntity.ok(Collections.singletonMap("enabled", entity.isEnabled()));
    }

    /**
     * Chạy thử nghiệm Tool với tham số JSON tùy chọn (Test Run Tool)
     */
    @PostMapping("/{id}/test")
    public ResponseEntity<?> testTool(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> arguments) {
        Optional<DynamicToolEntity> opt = toolRepository.findById(id);
        if (!opt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> args = arguments != null ? arguments : Collections.emptyMap();
        try {
            long startTime = System.currentTimeMillis();
            String output = toolExecutor.execute(opt.get(), args);
            long executionTimeMs = System.currentTimeMillis() - startTime;

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("toolName", opt.get().getName());
            result.put("output", output);
            result.put("executionTimeMs", executionTimeMs);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errResult = new HashMap<>();
            errResult.put("success", false);
            errResult.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errResult);
        }
    }

    private void updateEntityFromDto(DynamicToolEntity entity, ToolDto dto) {
        entity.setName(dto.getName().trim());
        entity.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : "");
        entity.setToolType(dto.getToolType());
        entity.setParametersSchema(dto.getParametersSchema() != null ? dto.getParametersSchema().trim() : "{}");
        entity.setConfigData(dto.getConfigData() != null ? dto.getConfigData().trim() : "");
        entity.setEnabled(dto.isEnabled());
    }

    private ToolDto toDto(DynamicToolEntity entity) {
        ToolDto dto = new ToolDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setToolType(entity.getToolType());
        dto.setParametersSchema(entity.getParametersSchema());
        dto.setConfigData(entity.getConfigData());
        dto.setEnabled(entity.isEnabled());
        dto.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : "");
        dto.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().toString() : "");
        return dto;
    }
}
