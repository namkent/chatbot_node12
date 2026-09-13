package com.chatbot.entity;

import javax.persistence.*;

@Entity
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_chat_messages_session_order", columnList = "session_id, order_idx ASC"),
        @Index(name = "idx_chat_messages_user", columnList = "user_id")
})
public class ChatMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", length = 64, nullable = false)
    private String sessionId;

    @Column(name = "user_id", length = 100, nullable = false)
    private String userId;

    @Column(name = "sender", length = 20, nullable = false)
    private String sender; // "user" hoặc "bot"

    @Column(name = "role", length = 30)
    private String role; // "user", "assistant", "system"

    @Column(name = "text", columnDefinition = "TEXT")
    private String text;

    @Column(name = "html", columnDefinition = "TEXT")
    private String html;

    @Column(name = "thinking", columnDefinition = "TEXT")
    private String thinking;

    @Column(name = "thinking_duration", length = 50)
    private String thinkingDuration;

    @Column(name = "response_time", length = 50)
    private String responseTime;

    @Column(name = "lang", length = 20)
    private String lang;

    @Column(name = "images_json", columnDefinition = "TEXT")
    private String imagesJson;

    @Column(name = "versions_json", columnDefinition = "TEXT")
    private String versionsJson;

    @Column(name = "order_idx")
    private Integer orderIdx;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = System.currentTimeMillis();
        }
    }

    public ChatMessageEntity() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getThinking() {
        return thinking;
    }

    public void setThinking(String thinking) {
        this.thinking = thinking;
    }

    public String getThinkingDuration() {
        return thinkingDuration;
    }

    public void setThinkingDuration(String thinkingDuration) {
        this.thinkingDuration = thinkingDuration;
    }

    public String getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(String responseTime) {
        this.responseTime = responseTime;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getImagesJson() {
        return imagesJson;
    }

    public void setImagesJson(String imagesJson) {
        this.imagesJson = imagesJson;
    }

    public String getVersionsJson() {
        return versionsJson;
    }

    public void setVersionsJson(String versionsJson) {
        this.versionsJson = versionsJson;
    }

    public Integer getOrderIdx() {
        return orderIdx;
    }

    public void setOrderIdx(Integer orderIdx) {
        this.orderIdx = orderIdx;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
}
