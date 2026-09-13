package com.chatbot.dto;

public class SearchResultDto {
    private Long chunkId;
    private Long documentId;
    private Integer chunkIndex;
    private String content;
    private double score; // 0.0 -> 1.0 (Similarity %)

    public SearchResultDto() {}

    public SearchResultDto(Long chunkId, Long documentId, Integer chunkIndex, String content, double score) {
        this.chunkId = chunkId;
        this.documentId = documentId;
        this.chunkIndex = chunkIndex;
        this.content = content;
        this.score = score;
    }

    public Long getChunkId() {
        return chunkId;
    }

    public void setChunkId(Long chunkId) {
        this.chunkId = chunkId;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
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
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
