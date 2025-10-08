package com.moodtracking.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moodtracking.dto.ConversationDTO;
import com.moodtracking.models.Conversation;
import com.moodtracking.models.User;
import com.moodtracking.repositories.ConversationRepository;

@Service
public class ConversationService {

    @Autowired
    private ConversationRepository conversationRepository;

    /**
     * Save a conversation message
     */
    @Transactional
    public ConversationDTO saveConversation(User user, String message, String sender, String sessionId) {
        Conversation conversation = new Conversation(user, message, sender, sessionId);
        Conversation saved = conversationRepository.save(conversation);
        return convertToDTO(saved);
    }

    /**
     * Get all conversations for a user
     */
    public List<ConversationDTO> getUserConversations(User user) {
        List<Conversation> conversations = conversationRepository.findByUserOrderByCreatedAtAsc(user);
        return conversations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get recent conversations (last N messages)
     */
    public List<ConversationDTO> getRecentConversations(User user, int limit) {
        List<Conversation> conversations = conversationRepository.findRecentByUser(user);
        return conversations.stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get conversations by session ID
     */
    public List<ConversationDTO> getConversationsBySession(String sessionId) {
        List<Conversation> conversations = conversationRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        return conversations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get conversation history for context (last 10 messages)
     */
    public String getConversationContext(User user, int messageCount) {
        List<Conversation> recent = conversationRepository.findRecentByUser(user);
        
        StringBuilder context = new StringBuilder();
        int count = 0;
        for (int i = recent.size() - 1; i >= 0 && count < messageCount; i--, count++) {
            Conversation conv = recent.get(i);
            context.insert(0, conv.getSender() + ": " + conv.getMessage() + "\n");
        }
        
        return context.toString();
    }

    /**
     * Delete conversations older than specified days
     */
    @Transactional
    public void deleteOldConversations(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        conversationRepository.deleteByCreatedAtBefore(cutoffDate);
    }

    /**
     * Delete all conversations for a user
     */
    @Transactional
    public void deleteUserConversations(User user) {
        List<Conversation> conversations = conversationRepository.findByUserOrderByCreatedAtAsc(user);
        conversationRepository.deleteAll(conversations);
    }

    /**
     * Get conversation count for a user
     */
    public long getConversationCount(User user) {
        return conversationRepository.countByUser(user);
    }

    /**
     * Convert entity to DTO
     */
    private ConversationDTO convertToDTO(Conversation conversation) {
        return new ConversationDTO(
            conversation.getId(),
            conversation.getMessage(),
            conversation.getSender(),
            conversation.getCreatedAt(),
            conversation.getSessionId()
        );
    }
}
