package com.moodtracking.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.moodtracking.models.Mood;
import com.moodtracking.models.User;

@Repository
public interface MoodRepository extends JpaRepository<Mood, Long> {
    
    List<Mood> findByUserOrderByTimestampDesc(User user);
    
    List<Mood> findByUserAndTimestampBetweenOrderByTimestampDesc(
        User user, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT m FROM Mood m WHERE m.user = :user AND m.timestamp >= :startDate ORDER BY m.timestamp DESC")
    List<Mood> findMoodsByUserAndDateAfter(@Param("user") User user, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(m) FROM Mood m WHERE m.user = :user AND m.timestamp >= :startDate")
    Long countMoodsByUserAndDateAfter(@Param("user") User user, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT m.moodType, COUNT(m) FROM Mood m WHERE m.user = :user AND m.timestamp >= :startDate GROUP BY m.moodType")
    List<Object[]> getMoodStatsByUser(@Param("user") User user, @Param("startDate") LocalDateTime startDate);
    
    List<Mood> findTop10ByUserOrderByTimestampDesc(User user);
}