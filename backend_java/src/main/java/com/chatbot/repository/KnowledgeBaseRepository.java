package com.chatbot.repository;

import com.chatbot.entity.KnowledgeBaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBaseEntity, Long> {
    List<KnowledgeBaseEntity> findAllByOrderByCreatedAtDesc();
}
