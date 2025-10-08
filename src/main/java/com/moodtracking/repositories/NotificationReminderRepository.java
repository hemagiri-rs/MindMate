package com.moodtracking.repositories;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.moodtracking.models.NotificationReminder;
import com.moodtracking.models.User;

@Repository
public interface NotificationReminderRepository extends JpaRepository<NotificationReminder, Long> {
    
    /**
     * Find all active reminders for a specific user
     */
    List<NotificationReminder> findByUserAndIsActiveTrue(User user);
    
    /**
     * Find all reminders for a specific user (active and inactive)
     */
    List<NotificationReminder> findByUser(User user);
    
    /**
     * Find all active reminders that should be sent at a specific time
     */
    @Query("SELECT nr FROM NotificationReminder nr WHERE nr.isActive = true AND nr.reminderTime = :time")
    List<NotificationReminder> findActiveRemindersByTime(@Param("time") LocalTime time);
    
    /**
     * Find all active reminders that should be sent within a time range
     */
    @Query("SELECT nr FROM NotificationReminder nr WHERE nr.isActive = true AND nr.reminderTime BETWEEN :startTime AND :endTime")
    List<NotificationReminder> findActiveRemindersByTimeRange(@Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime);
    
    /**
     * Find all active daily reminders
     */
    @Query("SELECT nr FROM NotificationReminder nr WHERE nr.isActive = true AND nr.frequency = 'DAILY'")
    List<NotificationReminder> findActiveDailyReminders();
    
    /**
     * Find user's reminder by frequency type
     */
    Optional<NotificationReminder> findByUserAndFrequencyAndIsActiveTrue(User user, NotificationReminder.ReminderFrequency frequency);
    
    /**
     * Count active reminders for a user
     */
    @Query("SELECT COUNT(nr) FROM NotificationReminder nr WHERE nr.user = :user AND nr.isActive = true")
    long countActiveRemindersByUser(@Param("user") User user);
    
    /**
     * Find reminders that need to be processed (considering timezone)
     */
    @Query("SELECT nr FROM NotificationReminder nr WHERE nr.isActive = true AND " +
           "FUNCTION('TIME', CONVERT_TZ(NOW(), 'UTC', nr.timezone)) BETWEEN :startTime AND :endTime")
    List<NotificationReminder> findRemindersToProcess(@Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime);
}