package com.moodtracking.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.moodtracking.dto.MoodEntryRequest;
import com.moodtracking.models.MoodEntry;
import com.moodtracking.models.User;
import com.moodtracking.repositories.MoodEntryRepository;

@Service
public class MoodEntryService {
    
    @Autowired
    private MoodEntryRepository moodEntryRepository;
    
    public MoodEntry createMoodEntry(MoodEntryRequest request, User user) {
        MoodEntry moodEntry = new MoodEntry(request.getMoodRating(), request.getNotes(), user);
        return moodEntryRepository.save(moodEntry);
    }
    
    public List<MoodEntry> getMoodEntriesForUser(User user) {
        return moodEntryRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    public List<MoodEntry> getMoodEntriesForUserInDateRange(User user, LocalDateTime startDate, LocalDateTime endDate) {
        return moodEntryRepository.findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(user, startDate, endDate);
    }
    
    public Optional<MoodEntry> getMoodEntryById(Long id) {
        return moodEntryRepository.findById(id);
    }
    
    public MoodEntry updateMoodEntry(Long id, MoodEntryRequest request, User user) {
        Optional<MoodEntry> optionalMoodEntry = moodEntryRepository.findById(id);
        if (optionalMoodEntry.isPresent()) {
            MoodEntry moodEntry = optionalMoodEntry.get();
            if (moodEntry.getUser().getId().equals(user.getId())) {
                moodEntry.setMoodRating(request.getMoodRating());
                moodEntry.setNotes(request.getNotes());
                return moodEntryRepository.save(moodEntry);
            } else {
                throw new RuntimeException("User not authorized to update this mood entry");
            }
        } else {
            throw new RuntimeException("Mood entry not found");
        }
    }
    
    public void deleteMoodEntry(Long id, User user) {
        Optional<MoodEntry> optionalMoodEntry = moodEntryRepository.findById(id);
        if (optionalMoodEntry.isPresent()) {
            MoodEntry moodEntry = optionalMoodEntry.get();
            if (moodEntry.getUser().getId().equals(user.getId())) {
                moodEntryRepository.delete(moodEntry);
            } else {
                throw new RuntimeException("User not authorized to delete this mood entry");
            }
        } else {
            throw new RuntimeException("Mood entry not found");
        }
    }
    
    public Double getAverageMoodRatingForUser(User user, LocalDateTime startDate) {
        return moodEntryRepository.getAverageMoodRatingForUser(user, startDate);
    }
    
    public List<String> generateRecommendations(User user) {
        List<MoodEntry> recentEntries = moodEntryRepository.findRecentMoodEntries(user);
        
        // Simple recommendation logic based on recent mood ratings
        if (recentEntries.isEmpty()) {
            return List.of("Start tracking your mood regularly to get personalized recommendations!");
        }
        
        double averageRating = recentEntries.stream()
                .limit(7) // Last 7 entries
                .mapToInt(MoodEntry::getMoodRating)
                .average()
                .orElse(5.0);
        
        if (averageRating <= 3) {
            return List.of(
                "Consider talking to a mental health professional",
                "Try meditation or deep breathing exercises",
                "Ensure you're getting enough sleep",
                "Engage in physical activity"
            );
        } else if (averageRating <= 6) {
            return List.of(
                "Try journaling about your feelings",
                "Connect with friends and family",
                "Consider a hobby you enjoy",
                "Take a walk in nature"
            );
        } else {
            return List.of(
                "Keep up the great work!",
                "Share your positive energy with others",
                "Try new activities to maintain your mood",
                "Practice gratitude"
            );
        }
    }
}