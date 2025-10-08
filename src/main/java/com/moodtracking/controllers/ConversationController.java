package com.moodtracking.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.moodtracking.dto.ConversationDTO;
import com.moodtracking.models.User;
import com.moodtracking.services.ConversationService;

@RestController
@RequestMapping("/api/conversations")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    /**
     * Save a conversation message
     */
    @PostMapping
    public ResponseEntity<?> saveConversation(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> payload) {
        
        try {
            User user = (User) userDetails;
            String message = payload.get("message");
            String sender = payload.get("sender");
            String sessionId = payload.getOrDefault("sessionId", UUID.randomUUID().toString());

            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Message is required"));
            }

            if (sender == null || (!sender.equals("user") && !sender.equals("bot"))) {
                return ResponseEntity.badRequest().body(Map.of("error", "Valid sender is required (user or bot)"));
            }

            ConversationDTO savedConversation = conversationService.saveConversation(
                user, message, sender, sessionId);

            return ResponseEntity.ok(savedConversation);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to save conversation: " + e.getMessage()));
        }
    }

    /**
     * Get all conversations for current user
     */
    @GetMapping
    public ResponseEntity<?> getUserConversations(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = (User) userDetails;
            List<ConversationDTO> conversations = conversationService.getUserConversations(user);
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch conversations: " + e.getMessage()));
        }
    }

    /**
     * Get recent conversations (last N messages)
     */
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentConversations(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "50") int limit) {
        
        try {
            User user = (User) userDetails;
            List<ConversationDTO> conversations = conversationService.getRecentConversations(user, limit);
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch recent conversations: " + e.getMessage()));
        }
    }

    /**
     * Get conversation context for AI (formatted string of recent messages)
     */
    @GetMapping("/context")
    public ResponseEntity<?> getConversationContext(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "10") int messageCount) {
        
        try {
            User user = (User) userDetails;
            String context = conversationService.getConversationContext(user, messageCount);
            return ResponseEntity.ok(Map.of("context", context));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch conversation context: " + e.getMessage()));
        }
    }

    /**
     * Get conversations by session ID
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<?> getConversationsBySession(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String sessionId) {
        
        try {
            List<ConversationDTO> conversations = conversationService.getConversationsBySession(sessionId);
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch session conversations: " + e.getMessage()));
        }
    }

    /**
     * Delete all conversations for current user
     */
    @DeleteMapping
    public ResponseEntity<?> deleteUserConversations(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = (User) userDetails;
            conversationService.deleteUserConversations(user);
            return ResponseEntity.ok(Map.of("message", "All conversations deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to delete conversations: " + e.getMessage()));
        }
    }

    /**
     * Get conversation statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getConversationStats(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = (User) userDetails;
            long count = conversationService.getConversationCount(user);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalMessages", count);
            stats.put("userId", user.getId());
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch conversation stats: " + e.getMessage()));
        }
    }
}
