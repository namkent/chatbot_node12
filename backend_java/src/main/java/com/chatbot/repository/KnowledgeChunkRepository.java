package com.chatbot.repository;

import com.chatbot.entity.KnowledgeChunkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeChunkRepository extends JpaRepository<KnowledgeChunkEntity, Long> {
    List<KnowledgeChunkEntity> findByKnowledgeBaseId(Long knowledgeBaseId);
    List<KnowledgeChunkEntity> findByDocumentIdOrderByChunkIndexAsc(Long documentId);
    long countByKnowledgeBaseId(Long knowledgeBaseId);
    void deleteByKnowledgeBaseId(Long knowledgeBaseId);
    void deleteByDocumentId(Long documentId);
}
