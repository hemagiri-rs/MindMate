package com.moodtracking.dto;

import java.time.LocalDate;

public class MoodTrendPoint {
    
    private LocalDate date;
    private Double averageMoodScore;
    private String period; // "day", "week", "month"
    private Integer moodCount;
    
    // Constructors
    public MoodTrendPoint() {}
    
    public MoodTrendPoint(LocalDate date, Double averageMoodScore, String period, Integer moodCount) {
        this.date = date;
        this.averageMoodScore = averageMoodScore;
        this.period = period;
        this.moodCount = moodCount;
    }
    
    // Getters and Setters
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public Double getAverageMoodScore() { return averageMoodScore; }
    public void setAverageMoodScore(Double averageMoodScore) { this.averageMoodScore = averageMoodScore; }
    
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    
    public Integer getMoodCount() { return moodCount; }
    public void setMoodCount(Integer moodCount) { this.moodCount = moodCount; }
}