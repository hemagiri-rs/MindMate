package com.moodtracking.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.moodtracking.dto.MoodAnalyticsResponse;
import com.moodtracking.dto.MoodResponse;
import com.moodtracking.dto.MoodTrendPoint;
import com.moodtracking.models.Mood.MoodType;
import com.moodtracking.models.User;

@Service
public class MoodAnalyticsService {
    
    @Autowired
    private MoodService moodService;
    
    // Mood scoring system (for trend analysis)
    private static final Map<MoodType, Integer> MOOD_SCORES = Map.of(
        MoodType.HAPPY, 9,
        MoodType.EXCITED, 8,
        MoodType.CONTENT, 7,
        MoodType.RELAXED, 6,
        MoodType.ENERGETIC, 8,
        MoodType.TIRED, 4,
        MoodType.ANXIOUS, 3,
        MoodType.STRESSED, 2,
        MoodType.SAD, 2,
        MoodType.ANGRY, 1
    );
    
    // Positive moods for streak calculation
    private static final Set<MoodType> POSITIVE_MOODS = Set.of(
        MoodType.HAPPY, MoodType.EXCITED, MoodType.CONTENT, 
        MoodType.RELAXED, MoodType.ENERGETIC
    );
    
    public MoodAnalyticsResponse generateMoodAnalytics(User user) {
        MoodAnalyticsResponse analytics = new MoodAnalyticsResponse();
        
        // Get all mood history
        List<MoodResponse> allMoods = moodService.getMoodHistory(user);
        
        if (allMoods.isEmpty()) {
            return generateEmptyAnalytics();
        }
        
        // Basic statistics
        analytics.setTotalMoodsTracked(allMoods.size());
        analytics.setDaysTracked(calculateDaysTracked(allMoods));
        
        // Current mood
        setCurrentMoodInfo(analytics, allMoods);
        
        // Mood distribution and frequency
        setMoodDistribution(analytics, allMoods);
        
        // Trends analysis
        setTrendAnalysis(analytics, allMoods);
        
        // Streak calculations
        setStreakAnalysis(analytics, allMoods);
        
        // Generate insights and recommendations
        generateInsights(analytics, allMoods);
        
        return analytics;
    }
    
    public MoodAnalyticsResponse generateMoodAnalytics(User user, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<MoodResponse> moods = moodService.getMoodHistoryInDateRange(user, startDate, LocalDateTime.now());
        
        if (moods.isEmpty()) {
            return generateEmptyAnalytics();
        }
        
        MoodAnalyticsResponse analytics = new MoodAnalyticsResponse();
        
        // Basic statistics
        analytics.setTotalMoodsTracked(moods.size());
        analytics.setDaysTracked(calculateDaysTracked(moods));
        
        // Current mood
        setCurrentMoodInfo(analytics, moods);
        
        // Mood distribution and frequency
        setMoodDistribution(analytics, moods);
        
        // Trends analysis
        setTrendAnalysis(analytics, moods);
        
        // Streak calculations
        setStreakAnalysis(analytics, moods);
        
        // Generate insights and recommendations
        generateInsights(analytics, moods);
        
        return analytics;
    }
    
    private MoodAnalyticsResponse generateEmptyAnalytics() {
        MoodAnalyticsResponse analytics = new MoodAnalyticsResponse();
        analytics.setTotalMoodsTracked(0);
        analytics.setDaysTracked(0);
        analytics.setInsights(Arrays.asList(
            "Start tracking your moods to see personalized analytics!",
            "Regular mood tracking helps identify patterns and trends.",
            "Your mood data will help provide better recommendations."
        ));
        analytics.setRecommendations(Arrays.asList(
            "Begin by adding your current mood",
            "Try to track your mood daily for better insights",
            "Add notes to your mood entries for more context"
        ));
        return analytics;
    }
    
    private void setCurrentMoodInfo(MoodAnalyticsResponse analytics, List<MoodResponse> moods) {
        if (!moods.isEmpty()) {
            MoodResponse latestMood = moods.get(0);
            analytics.setCurrentMood(latestMood.getMoodType());
            analytics.setCurrentMoodTimestamp(latestMood.getTimestamp());
        }
    }
    
    private void setMoodDistribution(MoodAnalyticsResponse analytics, List<MoodResponse> moods) {
        Map<MoodType, Long> moodCounts = moods.stream()
            .collect(Collectors.groupingBy(
                MoodResponse::getMoodType,
                Collectors.counting()
            ));
        
        analytics.setMoodCounts(moodCounts);
        
        // Calculate percentages
        int totalMoods = moods.size();
        Map<MoodType, Double> moodPercentages = moodCounts.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> (entry.getValue() * 100.0) / totalMoods
            ));
        analytics.setMoodPercentages(moodPercentages);
        
        // Find most frequent mood
        Optional<Map.Entry<MoodType, Long>> mostFrequent = moodCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue());
        
        if (mostFrequent.isPresent()) {
            analytics.setMostFrequentMood(mostFrequent.get().getKey());
            analytics.setMostFrequentMoodCount(mostFrequent.get().getValue());
            analytics.setMostFrequentMoodPercentage(moodPercentages.get(mostFrequent.get().getKey()));
        }
    }
    
    private void setTrendAnalysis(MoodAnalyticsResponse analytics, List<MoodResponse> moods) {
        // Weekly trend analysis
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        List<MoodResponse> weeklyMoods = moods.stream()
            .filter(mood -> mood.getTimestamp().isAfter(oneWeekAgo))
            .collect(Collectors.toList());
        
        if (!weeklyMoods.isEmpty()) {
            List<MoodTrendPoint> weeklyTrend = calculateTrendPoints(weeklyMoods, "day", 7);
            analytics.setWeeklyTrendData(weeklyTrend);
            analytics.setWeeklyTrend(calculateTrendDirection(weeklyTrend));
        }
        
        // Monthly trend analysis
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusDays(30);
        List<MoodResponse> monthlyMoods = moods.stream()
            .filter(mood -> mood.getTimestamp().isAfter(oneMonthAgo))
            .collect(Collectors.toList());
        
        if (!monthlyMoods.isEmpty()) {
            List<MoodTrendPoint> monthlyTrend = calculateTrendPoints(monthlyMoods, "week", 4);
            analytics.setMonthlyTrendData(monthlyTrend);
            analytics.setMonthlyTrend(calculateTrendDirection(monthlyTrend));
        }
    }
    
    private List<MoodTrendPoint> calculateTrendPoints(List<MoodResponse> moods, String period, int periods) {
        List<MoodTrendPoint> trendPoints = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
        
        for (int i = 0; i < periods; i++) {
            LocalDate startDate;
            LocalDate currentEndDate;
            
            if ("day".equals(period)) {
                startDate = endDate.minusDays(i + 1);
                currentEndDate = endDate.minusDays(i);
            } else { // week
                startDate = endDate.minusDays((i + 1) * 7);
                currentEndDate = endDate.minusDays(i * 7);
            }
            
            List<MoodResponse> periodMoods = moods.stream()
                .filter(mood -> {
                    LocalDate moodDate = mood.getTimestamp().toLocalDate();
                    return moodDate.isAfter(startDate) && moodDate.isBefore(currentEndDate.plusDays(1));
                })
                .collect(Collectors.toList());
            
            if (!periodMoods.isEmpty()) {
                double averageScore = periodMoods.stream()
                    .mapToInt(mood -> MOOD_SCORES.getOrDefault(mood.getMoodType(), 5))
                    .average()
                    .orElse(5.0);
                
                trendPoints.add(new MoodTrendPoint(currentEndDate, averageScore, period, periodMoods.size()));
            }
        }
        
        Collections.reverse(trendPoints);
        return trendPoints;
    }
    
    private String calculateTrendDirection(List<MoodTrendPoint> trendPoints) {
        if (trendPoints.size() < 2) {
            return "stable";
        }
        
        double firstHalfAvg = trendPoints.subList(0, trendPoints.size() / 2).stream()
            .mapToDouble(MoodTrendPoint::getAverageMoodScore)
            .average()
            .orElse(5.0);
        
        double secondHalfAvg = trendPoints.subList(trendPoints.size() / 2, trendPoints.size()).stream()
            .mapToDouble(MoodTrendPoint::getAverageMoodScore)
            .average()
            .orElse(5.0);
        
        double difference = secondHalfAvg - firstHalfAvg;
        
        if (difference > 0.5) {
            return "improving";
        } else if (difference < -0.5) {
            return "declining";
        } else {
            return "stable";
        }
    }
    
    private void setStreakAnalysis(MoodAnalyticsResponse analytics, List<MoodResponse> moods) {
        // Calculate positive and negative streaks
        int currentPositiveStreak = 0;
        int currentNegativeStreak = 0;
        int longestPositiveStreak = 0;
        int tempPositiveStreak = 0;
        
        for (MoodResponse mood : moods) {
            boolean isPositive = POSITIVE_MOODS.contains(mood.getMoodType());
            
            if (isPositive) {
                tempPositiveStreak++;
                if (currentNegativeStreak == 0) {
                    currentPositiveStreak++;
                }
                currentNegativeStreak = 0;
            } else {
                longestPositiveStreak = Math.max(longestPositiveStreak, tempPositiveStreak);
                tempPositiveStreak = 0;
                if (currentPositiveStreak == 0) {
                    currentNegativeStreak++;
                }
                currentPositiveStreak = 0;
            }
        }
        
        longestPositiveStreak = Math.max(longestPositiveStreak, tempPositiveStreak);
        
        analytics.setCurrentPositiveStreak(currentPositiveStreak);
        analytics.setLongestPositiveStreak(longestPositiveStreak);
        analytics.setCurrentNegativeStreak(currentNegativeStreak);
    }
    
    private void generateInsights(MoodAnalyticsResponse analytics, List<MoodResponse> moods) {
        List<String> insights = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        
        // Most frequent mood insight
        if (analytics.getMostFrequentMood() != null) {
            insights.add(String.format("Your most frequent mood is %s (%.1f%% of the time)",
                analytics.getMostFrequentMood().getDisplayName(),
                analytics.getMostFrequentMoodPercentage()));
        }
        
        // Trend insights
        if ("improving".equals(analytics.getWeeklyTrend())) {
            insights.add("Your mood has been improving over the past week! 📈");
            recommendations.add("Keep up the great work! Continue the activities that are helping your mood.");
        } else if ("declining".equals(analytics.getWeeklyTrend())) {
            insights.add("Your mood has been declining this week. 📉");
            recommendations.add("Consider talking to someone or trying stress-reduction activities.");
        } else {
            insights.add("Your mood has been stable this week.");
            recommendations.add("Consistency is good! Try some new activities to boost your mood.");
        }
        
        // Streak insights
        if (analytics.getCurrentPositiveStreak() > 0) {
            insights.add(String.format("You're on a %d-day positive mood streak! 🌟",
                analytics.getCurrentPositiveStreak()));
            recommendations.add("Amazing! Try to maintain this positive momentum.");
        } else if (analytics.getCurrentNegativeStreak() > 2) {
            insights.add(String.format("You've had %d consecutive days of challenging moods.",
                analytics.getCurrentNegativeStreak()));
            recommendations.add("Consider reaching out for support or trying mood-boosting activities.");
        }
        
        // Activity level insights
        double dailyAverage = (double) analytics.getTotalMoodsTracked() / analytics.getDaysTracked();
        if (dailyAverage > 2) {
            insights.add("You're very active in tracking your moods multiple times per day!");
        } else if (dailyAverage < 0.5) {
            insights.add("You track your mood occasionally.");
            recommendations.add("Try tracking your mood daily for better insights and patterns.");
        }
        
        // Mood diversity insight
        int uniqueMoods = analytics.getMoodCounts().size();
        if (uniqueMoods <= 3) {
            insights.add("You tend to experience a narrow range of moods.");
            recommendations.add("Explore activities that might introduce more variety in your emotional experiences.");
        } else if (uniqueMoods >= 7) {
            insights.add("You experience a wide range of emotions, which is completely normal!");
        }
        
        analytics.setInsights(insights);
        analytics.setRecommendations(recommendations);
    }
    
    private int calculateDaysTracked(List<MoodResponse> moods) {
        return (int) moods.stream()
            .map(mood -> mood.getTimestamp().toLocalDate())
            .distinct()
            .count();
    }
}