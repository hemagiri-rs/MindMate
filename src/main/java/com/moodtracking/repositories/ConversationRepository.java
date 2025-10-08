package com.moodtracking.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.moodtracking.models.Conversation;
import com.moodtracking.models.User;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    
    /**
     * Find all conversations for a user, ordered by creation time
     */
    List<Conversation> findByUserOrderByCreatedAtAsc(User user);
    
    /**
     * Find conversations by session ID
     */
    List<Conversation> findBySessionIdOrderByCreatedAtAsc(String sessionId);
    
    /**
     * Find recent conversations for a user (last N messages)
     */
    @Query("SELECT c FROM Conversation c WHERE c.user = :user ORDER BY c.createdAt DESC")
    List<Conversation> findRecentByUser(@Param("user") User user);
    
    /**
     * Find conversations within a time range
     */
    List<Conversation> findByUserAndCreatedAtBetweenOrderByCreatedAtAsc(
        User user, LocalDateTime start, LocalDateTime end);
    
    /**
     * Delete old conversations (data cleanup)
     */
    void deleteByCreatedAtBefore(LocalDateTime before);
    
    /**
     * Count conversations for a user
     */
    long countByUser(User user);
}
