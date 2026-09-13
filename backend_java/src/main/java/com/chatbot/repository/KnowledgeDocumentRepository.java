package com.chatbot.repository;

import com.chatbot.entity.KnowledgeDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocumentEntity, Long> {
    List<KnowledgeDocumentEntity> findByKnowledgeBaseIdOrderByCreatedAtDesc(Long knowledgeBaseId);
    long countByKnowledgeBaseId(Long knowledgeBaseId);
    void deleteByKnowledgeBaseId(Long knowledgeBaseId);
}
