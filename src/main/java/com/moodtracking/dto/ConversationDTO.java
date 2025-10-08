package com.moodtracking.dto;

import java.time.LocalDateTime;

public class ConversationDTO {
    private Long id;
    private String message;
    private String sender;
    private LocalDateTime createdAt;
    private String sessionId;

    // Constructors
    public ConversationDTO() {
    }

    public ConversationDTO(Long id, String message, String sender, LocalDateTime createdAt, String sessionId) {
        this.id = id;
        this.message = message;
        this.sender = sender;
        this.createdAt = createdAt;
        this.sessionId = sessionId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
