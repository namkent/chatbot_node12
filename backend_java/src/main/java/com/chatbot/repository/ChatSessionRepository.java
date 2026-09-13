package com.chatbot.repository;

import com.chatbot.entity.ChatSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSessionEntity, String> {
    List<ChatSessionEntity> findByUserIdOrderByUpdatedAtDesc(String userId);
    Optional<ChatSessionEntity> findByIdAndUserId(String id, String userId);
    void deleteByIdAndUserId(String id, String userId);
}
