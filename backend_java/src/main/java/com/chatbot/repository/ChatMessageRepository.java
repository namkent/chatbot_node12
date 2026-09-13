package com.chatbot.repository;

import com.chatbot.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    List<ChatMessageEntity> findBySessionIdOrderByOrderIdxAsc(String sessionId);
    List<ChatMessageEntity> findBySessionIdAndUserIdOrderByOrderIdxAsc(String sessionId, String userId);
    void deleteBySessionId(String sessionId);
    void deleteBySessionIdAndUserId(String sessionId, String userId);
}
