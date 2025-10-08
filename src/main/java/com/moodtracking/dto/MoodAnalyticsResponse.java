package com.moodtracking.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.moodtracking.models.Mood.MoodType;

public class MoodAnalyticsResponse {
    
    private LocalDateTime analysisDate;
    private Integer totalMoodsTracked;
    private Integer daysTracked;
    
    // Current mood information
    private MoodType currentMood;
    private LocalDateTime currentMoodTimestamp;
    
    // Most frequent mood
    private MoodType mostFrequentMood;
    private Long mostFrequentMoodCount;
    private Double mostFrequentMoodPercentage;
    
    // Mood distribution
    private Map<MoodType, Long> moodCounts;
    private Map<MoodType, Double> moodPercentages;
    
    // Trends and patterns
    private String weeklyTrend; // "improving", "declining", "stable"
    private String monthlyTrend;
    private List<MoodTrendPoint> weeklyTrendData;
    private List<MoodTrendPoint> monthlyTrendData;
    
    // Insights
    private List<String> insights;
    private List<String> recommendations;
    
    // Streak information
    private Integer currentPositiveStreak;
    private Integer longestPositiveStreak;
    private Integer currentNegativeStreak;
    
    // Constructors
    public MoodAnalyticsResponse() {
        this.analysisDate = LocalDateTime.now();
    }
    
    // Getters and Setters
    public LocalDateTime getAnalysisDate() { return analysisDate; }
    public void setAnalysisDate(LocalDateTime analysisDate) { this.analysisDate = analysisDate; }
    
    public Integer getTotalMoodsTracked() { return totalMoodsTracked; }
    public void setTotalMoodsTracked(Integer totalMoodsTracked) { this.totalMoodsTracked = totalMoodsTracked; }
    
    public Integer getDaysTracked() { return daysTracked; }
    public void setDaysTracked(Integer daysTracked) { this.daysTracked = daysTracked; }
    
    public MoodType getCurrentMood() { return currentMood; }
    public void setCurrentMood(MoodType currentMood) { this.currentMood = currentMood; }
    
    public LocalDateTime getCurrentMoodTimestamp() { return currentMoodTimestamp; }
    public void setCurrentMoodTimestamp(LocalDateTime currentMoodTimestamp) { this.currentMoodTimestamp = currentMoodTimestamp; }
    
    public MoodType getMostFrequentMood() { return mostFrequentMood; }
    public void setMostFrequentMood(MoodType mostFrequentMood) { this.mostFrequentMood = mostFrequentMood; }
    
    public Long getMostFrequentMoodCount() { return mostFrequentMoodCount; }
    public void setMostFrequentMoodCount(Long mostFrequentMoodCount) { this.mostFrequentMoodCount = mostFrequentMoodCount; }
    
    public Double getMostFrequentMoodPercentage() { return mostFrequentMoodPercentage; }
    public void setMostFrequentMoodPercentage(Double mostFrequentMoodPercentage) { this.mostFrequentMoodPercentage = mostFrequentMoodPercentage; }
    
    public Map<MoodType, Long> getMoodCounts() { return moodCounts; }
    public void setMoodCounts(Map<MoodType, Long> moodCounts) { this.moodCounts = moodCounts; }
    
    public Map<MoodType, Double> getMoodPercentages() { return moodPercentages; }
    public void setMoodPercentages(Map<MoodType, Double> moodPercentages) { this.moodPercentages = moodPercentages; }
    
    public String getWeeklyTrend() { return weeklyTrend; }
    public void setWeeklyTrend(String weeklyTrend) { this.weeklyTrend = weeklyTrend; }
    
    public String getMonthlyTrend() { return monthlyTrend; }
    public void setMonthlyTrend(String monthlyTrend) { this.monthlyTrend = monthlyTrend; }
    
    public List<MoodTrendPoint> getWeeklyTrendData() { return weeklyTrendData; }
    public void setWeeklyTrendData(List<MoodTrendPoint> weeklyTrendData) { this.weeklyTrendData = weeklyTrendData; }
    
    public List<MoodTrendPoint> getMonthlyTrendData() { return monthlyTrendData; }
    public void setMonthlyTrendData(List<MoodTrendPoint> monthlyTrendData) { this.monthlyTrendData = monthlyTrendData; }
    
    public List<String> getInsights() { return insights; }
    public void setInsights(List<String> insights) { this.insights = insights; }
    
    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
    
    public Integer getCurrentPositiveStreak() { return currentPositiveStreak; }
    public void setCurrentPositiveStreak(Integer currentPositiveStreak) { this.currentPositiveStreak = currentPositiveStreak; }
    
    public Integer getLongestPositiveStreak() { return longestPositiveStreak; }
    public void setLongestPositiveStreak(Integer longestPositiveStreak) { this.longestPositiveStreak = longestPositiveStreak; }
    
    public Integer getCurrentNegativeStreak() { return currentNegativeStreak; }
    public void setCurrentNegativeStreak(Integer currentNegativeStreak) { this.currentNegativeStreak = currentNegativeStreak; }
}