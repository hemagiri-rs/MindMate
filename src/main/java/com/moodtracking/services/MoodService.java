package com.moodtracking.services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.moodtracking.dto.MoodRequest;
import com.moodtracking.dto.MoodResponse;
import com.moodtracking.dto.MoodStatsResponse;
import com.moodtracking.models.Mood;
import com.moodtracking.models.Mood.MoodType;
import com.moodtracking.models.User;
import com.moodtracking.repositories.MoodRepository;

@Service
public class MoodService {
    
    @Autowired
    private MoodRepository moodRepository;
    
    public MoodResponse addMood(MoodRequest request, User user) {
        Mood mood = new Mood(request.getMoodType(), request.getNotes(), user);
        Mood savedMood = moodRepository.save(mood);
        
        return new MoodResponse(
            savedMood.getId(),
            savedMood.getMoodType(),
            savedMood.getNotes(),
            savedMood.getTimestamp(),
            savedMood.getUser().getUsername()
        );
    }
    
    public List<MoodResponse> getMoodHistory(User user) {
        List<Mood> moods = moodRepository.findByUserOrderByTimestampDesc(user);
        return moods.stream()
                .map(mood -> new MoodResponse(
                    mood.getId(),
                    mood.getMoodType(),
                    mood.getNotes(),
                    mood.getTimestamp(),
                    mood.getUser().getUsername()
                ))
                .collect(Collectors.toList());
    }
    
    public List<MoodResponse> getMoodHistoryInDateRange(User user, LocalDateTime startDate, LocalDateTime endDate) {
        List<Mood> moods = moodRepository.findByUserAndTimestampBetweenOrderByTimestampDesc(user, startDate, endDate);
        return moods.stream()
                .map(mood -> new MoodResponse(
                    mood.getId(),
                    mood.getMoodType(),
                    mood.getNotes(),
                    mood.getTimestamp(),
                    mood.getUser().getUsername()
                ))
                .collect(Collectors.toList());
    }
    
    public List<MoodResponse> getRecentMoods(User user) {
        List<Mood> moods = moodRepository.findTop10ByUserOrderByTimestampDesc(user);
        return moods.stream()
                .map(mood -> new MoodResponse(
                    mood.getId(),
                    mood.getMoodType(),
                    mood.getNotes(),
                    mood.getTimestamp(),
                    mood.getUser().getUsername()
                ))
                .collect(Collectors.toList());
    }
    
    public MoodStatsResponse getMoodStats(User user, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        
        // Get total count
        Long totalMoods = moodRepository.countMoodsByUserAndDateAfter(user, startDate);
        
        // Get mood counts by type
        List<Object[]> moodStats = moodRepository.getMoodStatsByUser(user, startDate);
        Map<MoodType, Long> moodCounts = new HashMap<>();
        
        for (Object[] stat : moodStats) {
            MoodType moodType = (MoodType) stat[0];
            Long count = (Long) stat[1];
            moodCounts.put(moodType, count);
        }
        
        // Find most frequent mood
        MoodType mostFrequentMood = moodCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
        
        return new MoodStatsResponse(totalMoods, moodCounts, mostFrequentMood, days);
    }
    
    public Optional<MoodResponse> getMoodById(Long id, User user) {
        Optional<Mood> mood = moodRepository.findById(id);
        if (mood.isPresent() && mood.get().getUser().getId().equals(user.getId())) {
            Mood m = mood.get();
            return Optional.of(new MoodResponse(
                m.getId(),
                m.getMoodType(),
                m.getNotes(),
                m.getTimestamp(),
                m.getUser().getUsername()
            ));
        }
        return Optional.empty();
    }
    
    public boolean deleteMood(Long id, User user) {
        Optional<Mood> mood = moodRepository.findById(id);
        if (mood.isPresent() && mood.get().getUser().getId().equals(user.getId())) {
            moodRepository.delete(mood.get());
            return true;
        }
        return false;
    }
    
    public Optional<MoodResponse> updateMood(Long id, MoodRequest request, User user) {
        Optional<Mood> moodOpt = moodRepository.findById(id);
        if (moodOpt.isPresent() && moodOpt.get().getUser().getId().equals(user.getId())) {
            Mood mood = moodOpt.get();
            mood.setMoodType(request.getMoodType());
            mood.setNotes(request.getNotes());
            
            Mood updatedMood = moodRepository.save(mood);
            return Optional.of(new MoodResponse(
                updatedMood.getId(),
                updatedMood.getMoodType(),
                updatedMood.getNotes(),
                updatedMood.getTimestamp(),
                updatedMood.getUser().getUsername()
            ));
        }
        return Optional.empty();
    }
}