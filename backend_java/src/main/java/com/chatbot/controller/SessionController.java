package com.chatbot.controller;

import com.chatbot.dto.SessionDetailDto;
import com.chatbot.dto.SessionDto;
import com.chatbot.service.SessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    /**
     * Lấy danh sách toàn bộ phiên chat của người dùng
     * GET /api/sessions
     */
    @GetMapping
    public ResponseEntity<List<SessionDto>> getSessions(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestParam(value = "userId", required = false) String paramUserId) {
        String userId = (headerUserId != null && !headerUserId.trim().isEmpty())
                ? headerUserId.trim()
                : ((paramUserId != null && !paramUserId.trim().isEmpty()) ? paramUserId.trim() : SessionService.DEFAULT_USER_ID);

        List<SessionDto> list = sessionService.getUserSessions(userId);
        return ResponseEntity.ok(list);
    }

    /**
     * Lấy chi tiết một phiên chat kèm danh sách tin nhắn
     * GET /api/sessions/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getSessionDetail(
            @PathVariable("id") String id,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestParam(value = "userId", required = false) String paramUserId) {
        String userId = (headerUserId != null && !headerUserId.trim().isEmpty())
                ? headerUserId.trim()
                : ((paramUserId != null && !paramUserId.trim().isEmpty()) ? paramUserId.trim() : SessionService.DEFAULT_USER_ID);

        SessionDetailDto detail = sessionService.getSessionDetail(id, userId);
        if (detail == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "Không tìm thấy phiên chat id=" + id));
        }
        return ResponseEntity.ok(detail);
    }

    /**
     * Lưu hoặc cập nhật phiên chat và tin nhắn vào H2 DB
     * POST /api/sessions
     */
    @PostMapping
    public ResponseEntity<SessionDetailDto> saveOrUpdateSession(
            @RequestBody SessionDetailDto dto,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {
        String userId = (headerUserId != null && !headerUserId.trim().isEmpty())
                ? headerUserId.trim()
                : ((dto.getUserId() != null && !dto.getUserId().trim().isEmpty()) ? dto.getUserId().trim() : SessionService.DEFAULT_USER_ID);

        SessionDetailDto saved = sessionService.saveOrUpdateSession(dto, userId);
        return ResponseEntity.ok(saved);
    }

    /**
     * Đổi tên tiêu đề phiên chat
     * PUT /api/sessions/{id}/title
     */
    @PutMapping("/{id}/title")
    public ResponseEntity<?> renameSession(
            @PathVariable("id") String id,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {
        String userId = (headerUserId != null && !headerUserId.trim().isEmpty())
                ? headerUserId.trim()
                : SessionService.DEFAULT_USER_ID;

        String newTitle = body != null ? body.get("title") : "";
        SessionDto updated = sessionService.renameSession(id, newTitle, userId);
        if (updated == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "Không tìm thấy phiên chat id=" + id));
        }
        return ResponseEntity.ok(updated);
    }

    /**
     * Xoá phiên chat và toàn bộ nội dung tin nhắn
     * DELETE /api/sessions/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSession(
            @PathVariable("id") String id,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {
        String userId = (headerUserId != null && !headerUserId.trim().isEmpty())
                ? headerUserId.trim()
                : SessionService.DEFAULT_USER_ID;

        boolean deleted = sessionService.deleteSession(id, userId);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "Không tìm thấy phiên chat id=" + id));
        }
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }
}
