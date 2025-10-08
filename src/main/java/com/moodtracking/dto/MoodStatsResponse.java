package com.moodtracking.dto;

import java.util.Map;

import com.moodtracking.models.Mood.MoodType;

public class MoodStatsResponse {
    
    private Long totalMoods;
    private Map<MoodType, Long> moodCounts;
    private MoodType mostFrequentMood;
    private Integer daysTracked;
    
    // Constructors
    public MoodStatsResponse() {}
    
    public MoodStatsResponse(Long totalMoods, Map<MoodType, Long> moodCounts, 
                           MoodType mostFrequentMood, Integer daysTracked) {
        this.totalMoods = totalMoods;
        this.moodCounts = moodCounts;
        this.mostFrequentMood = mostFrequentMood;
        this.daysTracked = daysTracked;
    }
    
    // Getters and Setters
    public Long getTotalMoods() { return totalMoods; }
    public void setTotalMoods(Long totalMoods) { this.totalMoods = totalMoods; }
    
    public Map<MoodType, Long> getMoodCounts() { return moodCounts; }
    public void setMoodCounts(Map<MoodType, Long> moodCounts) { this.moodCounts = moodCounts; }
    
    public MoodType getMostFrequentMood() { return mostFrequentMood; }
    public void setMostFrequentMood(MoodType mostFrequentMood) { this.mostFrequentMood = mostFrequentMood; }
    
    public Integer getDaysTracked() { return daysTracked; }
    public void setDaysTracked(Integer daysTracked) { this.daysTracked = daysTracked; }
}