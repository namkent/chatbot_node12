package com.chatbot.service;

import com.chatbot.dto.SessionDetailDto;
import com.chatbot.dto.SessionDto;
import com.chatbot.entity.ChatMessageEntity;
import com.chatbot.entity.ChatSessionEntity;
import com.chatbot.repository.ChatMessageRepository;
import com.chatbot.repository.ChatSessionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);

    public static final String DEFAULT_USER_ID = "nam.dovan";
    public static final String DEFAULT_USER_NAME = "Do Van Nam";

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final ObjectMapper objectMapper;

    public SessionService(ChatSessionRepository sessionRepository,
                          ChatMessageRepository messageRepository,
                          ObjectMapper objectMapper) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.objectMapper = objectMapper;
    }

    private String resolveUserId(String userId) {
        return (userId != null && !userId.trim().isEmpty()) ? userId.trim() : DEFAULT_USER_ID;
    }

    private String resolveUserName(String userName) {
        return (userName != null && !userName.trim().isEmpty()) ? userName.trim() : DEFAULT_USER_NAME;
    }

    /**
     * Lấy toàn bộ danh sách phiên chat theo user, sắp xếp mới nhất lên đầu
     */
    @Transactional(readOnly = true)
    public List<SessionDto> getUserSessions(String userId) {
        String uid = resolveUserId(userId);
        List<ChatSessionEntity> entities = sessionRepository.findByUserIdOrderByUpdatedAtDesc(uid);
        List<SessionDto> dtos = new ArrayList<>();
        for (ChatSessionEntity entity : entities) {
            SessionDto dto = toSessionDto(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    /**
     * Lấy chi tiết phiên chat gồm toàn bộ danh sách tin nhắn
     */
    @Transactional(readOnly = true)
    public SessionDetailDto getSessionDetail(String sessionId, String userId) {
        if (sessionId == null || sessionId.trim().isEmpty()) return null;
        String uid = resolveUserId(userId);

        ChatSessionEntity entity = sessionRepository.findById(sessionId.trim()).orElse(null);
        if (entity == null) return null;

        List<ChatMessageEntity> messageEntities = messageRepository.findBySessionIdOrderByOrderIdxAsc(entity.getId());

        SessionDetailDto detailDto = new SessionDetailDto();
        copyEntityToDto(entity, detailDto);
        detailDto.setMessageCount(messageEntities.size());

        List<Map<String, Object>> messageList = new ArrayList<>();
        List<Map<String, Object>> apiMessagesHistory = new ArrayList<>();

        if (entity.getSystemPrompt() != null && !entity.getSystemPrompt().trim().isEmpty()) {
            Map<String, Object> sysMsg = new HashMap<>();
            sysMsg.put("role", "system");
            sysMsg.put("content", entity.getSystemPrompt());
            apiMessagesHistory.add(sysMsg);
        }

        for (ChatMessageEntity me : messageEntities) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", me.getId());
            m.put("sender", me.getSender());
            m.put("role", me.getRole());
            m.put("text", me.getText() != null ? me.getText() : "");
            m.put("html", me.getHtml() != null ? me.getHtml() : "");
            if (me.getThinking() != null) m.put("thinking", me.getThinking());
            if (me.getThinkingDuration() != null) m.put("thinkingDuration", me.getThinkingDuration());
            if (me.getResponseTime() != null) m.put("responseTime", me.getResponseTime());
            if (me.getLang() != null) m.put("lang", me.getLang());
            m.put("createdAt", me.getCreatedAt());

            if (me.getImagesJson() != null && !me.getImagesJson().trim().isEmpty()) {
                try {
                    List<Object> imgs = objectMapper.readValue(me.getImagesJson(), new TypeReference<List<Object>>() {});
                    m.put("images", imgs);
                } catch (Exception ignored) {}
            }

            if (me.getVersionsJson() != null && !me.getVersionsJson().trim().isEmpty()) {
                try {
                    List<Object> vers = objectMapper.readValue(me.getVersionsJson(), new TypeReference<List<Object>>() {});
                    m.put("versions", vers);
                    m.put("currentVersionIdx", vers.size() - 1);
                } catch (Exception ignored) {}
            }

            messageList.add(m);

            if (me.getRole() != null && !me.getRole().equalsIgnoreCase("system")) {
                Map<String, Object> apiMsg = new HashMap<>();
                apiMsg.put("role", me.getRole());
                apiMsg.put("content", me.getText());
                apiMessagesHistory.add(apiMsg);
            }
        }

        detailDto.setMessageList(messageList);
        detailDto.setApiMessagesHistory(apiMessagesHistory);
        return detailDto;
    }

    /**
     * Tạo mới hoặc lưu cập nhật một phiên chat và các tin nhắn vào H2 DB
     */
    @Transactional
    public SessionDetailDto saveOrUpdateSession(SessionDetailDto dto, String userId) {
        String uid = resolveUserId(userId != null ? userId : dto.getUserId());
        String uname = resolveUserName(dto.getUserName());

        String sessionId = (dto.getId() != null && !dto.getId().trim().isEmpty())
                ? dto.getId().trim()
                : UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        ChatSessionEntity entity = sessionRepository.findById(sessionId).orElse(null);
        boolean isNew = (entity == null);

        if (isNew) {
            entity = new ChatSessionEntity();
            entity.setId(sessionId);
            entity.setUserId(uid);
            entity.setUserName(uname);
            entity.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : System.currentTimeMillis());
        }

        String title = dto.getTitle();
        if (title == null || title.trim().isEmpty()) {
            title = "Cuộc trò chuyện mới";
        }
        entity.setTitle(title.trim());

        String emoji = dto.getEmoji();
        if (emoji == null || emoji.trim().isEmpty()) {
            emoji = extractLeadingEmoji(title);
        }
        entity.setEmoji(emoji);

        if (dto.getIsCustomRenamed() != null) {
            entity.setIsCustomRenamed(dto.getIsCustomRenamed());
        }
        if (dto.getModel() != null) {
            entity.setModel(dto.getModel());
        }
        if (dto.getSystemPrompt() != null) {
            entity.setSystemPrompt(dto.getSystemPrompt());
        }
        entity.setUpdatedAt(System.currentTimeMillis());

        ChatSessionEntity savedSession = sessionRepository.save(entity);

        // Lưu trữ tin nhắn vào bảng chat_messages
        if (dto.getMessageList() != null) {
            messageRepository.deleteBySessionId(savedSession.getId());

            List<ChatMessageEntity> toInsert = new ArrayList<>();
            int idx = 0;
            for (Map<String, Object> m : dto.getMessageList()) {
                ChatMessageEntity cme = new ChatMessageEntity();
                cme.setSessionId(savedSession.getId());
                cme.setUserId(uid);
                cme.setSender(m.get("sender") != null ? m.get("sender").toString() : "user");
                cme.setRole(m.get("role") != null ? m.get("role").toString() : (cme.getSender().equals("user") ? "user" : "assistant"));
                cme.setText(m.get("text") != null ? m.get("text").toString() : "");
                cme.setHtml(m.get("html") != null ? m.get("html").toString() : "");
                if (m.get("thinking") != null) cme.setThinking(m.get("thinking").toString());
                if (m.get("thinkingDuration") != null) cme.setThinkingDuration(m.get("thinkingDuration").toString());
                if (m.get("responseTime") != null) cme.setResponseTime(m.get("responseTime").toString());
                if (m.get("lang") != null) cme.setLang(m.get("lang").toString());

                if (m.containsKey("images") && m.get("images") != null) {
                    try {
                        cme.setImagesJson(objectMapper.writeValueAsString(m.get("images")));
                    } catch (Exception ignored) {}
                }

                if (m.containsKey("versions") && m.get("versions") != null) {
                    try {
                        cme.setVersionsJson(objectMapper.writeValueAsString(m.get("versions")));
                    } catch (Exception ignored) {}
                }

                cme.setOrderIdx(idx++);
                if (m.get("createdAt") != null && m.get("createdAt") instanceof Number) {
                    cme.setCreatedAt(((Number) m.get("createdAt")).longValue());
                } else {
                    cme.setCreatedAt(System.currentTimeMillis());
                }
                toInsert.add(cme);
            }
            if (!toInsert.isEmpty()) {
                messageRepository.saveAll(toInsert);
            }
        }

        log.info("[H2 DB Session]: Đã lưu phiên chat id='{}', title='{}', messages={}", savedSession.getId(), savedSession.getTitle(), dto.getMessageList() != null ? dto.getMessageList().size() : 0);
        return getSessionDetail(savedSession.getId(), uid);
    }

    /**
     * Đổi tên tiêu đề phiên chat
     */
    @Transactional
    public SessionDto renameSession(String sessionId, String newTitle, String userId) {
        if (sessionId == null || sessionId.trim().isEmpty()) return null;
        String uid = resolveUserId(userId);

        ChatSessionEntity entity = sessionRepository.findById(sessionId.trim()).orElse(null);
        if (entity == null) return null;

        String title = (newTitle != null && !newTitle.trim().isEmpty()) ? newTitle.trim() : "Cuộc trò chuyện";
        entity.setTitle(title);
        entity.setEmoji(extractLeadingEmoji(title));
        entity.setIsCustomRenamed(true);
        entity.setUpdatedAt(System.currentTimeMillis());

        ChatSessionEntity saved = sessionRepository.save(entity);
        return toSessionDto(saved);
    }

    /**
     * Xoá phiên chat và toàn bộ nội dung tin nhắn liên quan
     */
    @Transactional
    public boolean deleteSession(String sessionId, String userId) {
        if (sessionId == null || sessionId.trim().isEmpty()) return false;
        String uid = resolveUserId(userId);

        ChatSessionEntity entity = sessionRepository.findById(sessionId.trim()).orElse(null);
        if (entity != null) {
            messageRepository.deleteBySessionId(entity.getId());
            sessionRepository.delete(entity);
            log.info("[H2 DB Session]: Đã xoá phiên chat id='{}'", sessionId);
            return true;
        }
        return false;
    }

    private SessionDto toSessionDto(ChatSessionEntity entity) {
        SessionDto dto = new SessionDto();
        copyEntityToDto(entity, dto);
        return dto;
    }

    private void copyEntityToDto(ChatSessionEntity entity, SessionDto dto) {
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setUserName(entity.getUserName());
        dto.setTitle(entity.getTitle());
        dto.setEmoji(entity.getEmoji());
        dto.setIsCustomRenamed(entity.getIsCustomRenamed());
        dto.setModel(entity.getModel());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
    }

    private String extractLeadingEmoji(String text) {
        if (text == null || text.trim().isEmpty()) return "💬";
        String clean = text.trim();
        if (clean.length() >= 2 && Character.isSurrogatePair(clean.charAt(0), clean.charAt(1))) {
            return clean.substring(0, 2);
        } else if (clean.length() >= 1 && (clean.charAt(0) >= 0x2600 && clean.charAt(0) <= 0x27BF)) {
            return clean.substring(0, 1);
        }
        return "💬";
    }
}
