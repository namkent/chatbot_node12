package com.chatbot.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "knowledge_chunks", indexes = {
        @Index(name = "idx_chunk_kb", columnList = "knowledge_base_id"),
        @Index(name = "idx_chunk_doc", columnList = "document_id")
})
public class KnowledgeChunkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(name = "knowledge_base_id", nullable = false)
    private Long knowledgeBaseId;

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    @Lob
    @Column(columnDefinition = "CLOB", nullable = false)
    private String content;

    @Column(name = "char_count")
    private Integer charCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.content != null) {
            this.charCount = this.content.length();
        }
    }

    public KnowledgeChunkEntity() {}

    public KnowledgeChunkEntity(Long documentId, Long knowledgeBaseId, Integer chunkIndex, String content) {
        this.documentId = documentId;
        this.knowledgeBaseId = knowledgeBaseId;
        this.chunkIndex = chunkIndex;
        this.content = content;
        if (content != null) {
            this.charCount = content.length();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public Long getKnowledgeBaseId() {
        return knowledgeBaseId;
    }

    public void setKnowledgeBaseId(Long knowledgeBaseId) {
        this.knowledgeBaseId = knowledgeBaseId;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(Integer chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        if (content != null) {
            this.charCount = content.length();
        }
    }

    public Integer getCharCount() {
        return charCount;
    }

    public void setCharCount(Integer charCount) {
        this.charCount = charCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
