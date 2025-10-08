package com.moodtracking.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.moodtracking.dto.MoodAnalyticsResponse;
import com.moodtracking.models.User;
import com.moodtracking.services.MoodAnalyticsService;

@RestController
@RequestMapping("/analytics")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AnalyticsController {
    
    @Autowired
    private MoodAnalyticsService moodAnalyticsService;
    
    @GetMapping("/mood")
    public ResponseEntity<?> getMoodAnalytics(@RequestParam(defaultValue = "0") int days,
                                            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            MoodAnalyticsResponse analytics;
            
            if (days > 0) {
                analytics = moodAnalyticsService.generateMoodAnalytics(user, days);
            } else {
                analytics = moodAnalyticsService.generateMoodAnalytics(user);
            }
            
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error generating mood analytics: " + e.getMessage());
        }
    }
    
    @GetMapping("/mood/summary")
    public ResponseEntity<?> getMoodSummary(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            MoodAnalyticsResponse analytics = moodAnalyticsService.generateMoodAnalytics(user, 30);
            
            // Create a simplified summary response
            MoodSummaryResponse summary = new MoodSummaryResponse(
                analytics.getTotalMoodsTracked(),
                analytics.getDaysTracked(),
                analytics.getCurrentMood(),
                analytics.getMostFrequentMood(),
                analytics.getWeeklyTrend(),
                analytics.getCurrentPositiveStreak(),
                analytics.getInsights() != null && !analytics.getInsights().isEmpty() 
                    ? analytics.getInsights().get(0) : "No insights available yet."
            );
            
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error generating mood summary: " + e.getMessage());
        }
    }
    
    @GetMapping("/mood/trends")
    public ResponseEntity<?> getMoodTrends(@RequestParam(defaultValue = "30") int days,
                                         Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            MoodAnalyticsResponse analytics = moodAnalyticsService.generateMoodAnalytics(user, days);
            
            // Create a trends-focused response
            MoodTrendsResponse trends = new MoodTrendsResponse(
                analytics.getWeeklyTrend(),
                analytics.getMonthlyTrend(),
                analytics.getWeeklyTrendData(),
                analytics.getMonthlyTrendData()
            );
            
            return ResponseEntity.ok(trends);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error generating mood trends: " + e.getMessage());
        }
    }
    
    // Inner classes for simplified responses
    public static class MoodSummaryResponse {
        private final Integer totalMoods;
        private final Integer daysTracked;
        private final com.moodtracking.models.Mood.MoodType currentMood;
        private final com.moodtracking.models.Mood.MoodType mostFrequentMood;
        private final String weeklyTrend;
        private final Integer currentPositiveStreak;
        private final String topInsight;
        
        public MoodSummaryResponse(Integer totalMoods, Integer daysTracked, 
                                 com.moodtracking.models.Mood.MoodType currentMood,
                                 com.moodtracking.models.Mood.MoodType mostFrequentMood,
                                 String weeklyTrend, Integer currentPositiveStreak, String topInsight) {
            this.totalMoods = totalMoods;
            this.daysTracked = daysTracked;
            this.currentMood = currentMood;
            this.mostFrequentMood = mostFrequentMood;
            this.weeklyTrend = weeklyTrend;
            this.currentPositiveStreak = currentPositiveStreak;
            this.topInsight = topInsight;
        }
        
        // Getters
        public Integer getTotalMoods() { return totalMoods; }
        public Integer getDaysTracked() { return daysTracked; }
        public com.moodtracking.models.Mood.MoodType getCurrentMood() { return currentMood; }
        public com.moodtracking.models.Mood.MoodType getMostFrequentMood() { return mostFrequentMood; }
        public String getWeeklyTrend() { return weeklyTrend; }
        public Integer getCurrentPositiveStreak() { return currentPositiveStreak; }
        public String getTopInsight() { return topInsight; }
    }
    
    public static class MoodTrendsResponse {
        private final String weeklyTrend;
        private final String monthlyTrend;
        private final java.util.List<com.moodtracking.dto.MoodTrendPoint> weeklyTrendData;
        private final java.util.List<com.moodtracking.dto.MoodTrendPoint> monthlyTrendData;
        
        public MoodTrendsResponse(String weeklyTrend, String monthlyTrend,
                                java.util.List<com.moodtracking.dto.MoodTrendPoint> weeklyTrendData,
                                java.util.List<com.moodtracking.dto.MoodTrendPoint> monthlyTrendData) {
            this.weeklyTrend = weeklyTrend;
            this.monthlyTrend = monthlyTrend;
            this.weeklyTrendData = weeklyTrendData;
            this.monthlyTrendData = monthlyTrendData;
        }
        
        // Getters
        public String getWeeklyTrend() { return weeklyTrend; }
        public String getMonthlyTrend() { return monthlyTrend; }
        public java.util.List<com.moodtracking.dto.MoodTrendPoint> getWeeklyTrendData() { return weeklyTrendData; }
        public java.util.List<com.moodtracking.dto.MoodTrendPoint> getMonthlyTrendData() { return monthlyTrendData; }
    }
}