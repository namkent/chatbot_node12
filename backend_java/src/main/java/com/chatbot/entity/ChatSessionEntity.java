package com.chatbot.entity;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "chat_sessions", indexes = {
        @Index(name = "idx_chat_sessions_user_updated", columnList = "user_id, updated_at DESC")
})
public class ChatSessionEntity {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Column(name = "user_id", length = 100, nullable = false)
    private String userId;

    @Column(name = "user_name", length = 150)
    private String userName;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "emoji", length = 20)
    private String emoji;

    @Column(name = "is_custom_renamed")
    private Boolean isCustomRenamed = false;

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "system_prompt", columnDefinition = "TEXT")
    private String systemPrompt;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null || this.id.trim().isEmpty()) {
            this.id = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        }
        long now = System.currentTimeMillis();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = System.currentTimeMillis();
    }

    public ChatSessionEntity() {}

    public ChatSessionEntity(String id, String userId, String userName, String title, String emoji) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.title = title;
        this.emoji = emoji;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public Boolean getIsCustomRenamed() {
        return isCustomRenamed;
    }

    public void setIsCustomRenamed(Boolean customRenamed) {
        isCustomRenamed = customRenamed;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
