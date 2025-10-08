package com.moodtracking.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "moods")
public class Mood {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "mood_type")
    private MoodType moodType;
    
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    
    @Column(name = "notes", length = 500)
    private String notes;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
    
    // Constructors
    public Mood() {}
    
    public Mood(MoodType moodType, User user) {
        this.moodType = moodType;
        this.user = user;
    }
    
    public Mood(MoodType moodType, String notes, User user) {
        this.moodType = moodType;
        this.notes = notes;
        this.user = user;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public MoodType getMoodType() { return moodType; }
    public void setMoodType(MoodType moodType) { this.moodType = moodType; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    // Enum for mood types
    public enum MoodType {
        HAPPY("Happy"),
        SAD("Sad"),
        STRESSED("Stressed"),
        RELAXED("Relaxed"),
        ANXIOUS("Anxious"),
        EXCITED("Excited"),
        ANGRY("Angry"),
        CONTENT("Content"),
        TIRED("Tired"),
        ENERGETIC("Energetic");
        
        private final String displayName;
        
        MoodType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}