package com.moodtracking.dto;

import java.time.LocalDateTime;

import com.moodtracking.models.Mood.MoodType;

public class MoodResponse {
    
    private Long id;
    private MoodType moodType;
    private String notes;
    private LocalDateTime timestamp;
    private String username;
    
    // Constructors
    public MoodResponse() {}
    
    public MoodResponse(Long id, MoodType moodType, String notes, LocalDateTime timestamp, String username) {
        this.id = id;
        this.moodType = moodType;
        this.notes = notes;
        this.timestamp = timestamp;
        this.username = username;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public MoodType getMoodType() { return moodType; }
    public void setMoodType(MoodType moodType) { this.moodType = moodType; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}