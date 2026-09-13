package com.chatbot.repository;

import com.chatbot.entity.DynamicToolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DynamicToolRepository extends JpaRepository<DynamicToolEntity, Long> {
    List<DynamicToolEntity> findByEnabledTrue();
    Optional<DynamicToolEntity> findByName(String name);
    boolean existsByName(String name);
}
