package com.moodtracking.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class MoodEntryRequest {
    
    @NotNull
    @Min(1)
    @Max(10)
    private Integer moodRating;
    
    private String notes;
    
    // Constructors
    public MoodEntryRequest() {}
    
    public MoodEntryRequest(Integer moodRating, String notes) {
        this.moodRating = moodRating;
        this.notes = notes;
    }
    
    // Getters and Setters
    public Integer getMoodRating() { return moodRating; }
    public void setMoodRating(Integer moodRating) { this.moodRating = moodRating; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}