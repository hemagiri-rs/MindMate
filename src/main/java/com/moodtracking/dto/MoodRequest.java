package com.moodtracking.dto;

import com.moodtracking.models.Mood.MoodType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class MoodRequest {
    
    @NotNull
    private MoodType moodType;
    
    @Size(max = 500)
    private String notes;
    
    // Constructors
    public MoodRequest() {}
    
    public MoodRequest(MoodType moodType, String notes) {
        this.moodType = moodType;
        this.notes = notes;
    }
    
    // Getters and Setters
    public MoodType getMoodType() { return moodType; }
    public void setMoodType(MoodType moodType) { this.moodType = moodType; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}