package com.moodtracking.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.moodtracking.models.MoodEntry;
import com.moodtracking.models.User;

@Repository
public interface MoodEntryRepository extends JpaRepository<MoodEntry, Long> {
    
    List<MoodEntry> findByUserOrderByCreatedAtDesc(User user);
    
    List<MoodEntry> findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(
        User user, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT AVG(m.moodRating) FROM MoodEntry m WHERE m.user = :user AND m.createdAt >= :startDate")
    Double getAverageMoodRatingForUser(@Param("user") User user, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT m FROM MoodEntry m WHERE m.user = :user ORDER BY m.createdAt DESC")
    List<MoodEntry> findRecentMoodEntries(@Param("user") User user);
}