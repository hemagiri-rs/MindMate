package com.moodtracking.services;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moodtracking.dto.NotificationReminderResponse;
import com.moodtracking.dto.ScheduleReminderRequest;
import com.moodtracking.models.NotificationReminder;
import com.moodtracking.models.User;
import com.moodtracking.repositories.NotificationReminderRepository;

@Service
@Transactional
public class NotificationService {
    
    @Autowired
    private NotificationReminderRepository notificationReminderRepository;
    
    /**
     * Schedule a new reminder for a user
     */
    public NotificationReminderResponse scheduleReminder(User user, ScheduleReminderRequest request) {
        // Check if user already has a reminder with the same frequency
        Optional<NotificationReminder> existingReminder = notificationReminderRepository
            .findByUserAndFrequencyAndIsActiveTrue(user, request.getFrequency());
        
        if (existingReminder.isPresent()) {
            // Update existing reminder
            NotificationReminder reminder = existingReminder.get();
            reminder.setReminderTime(request.getReminderTime());
            reminder.setMessage(request.getMessage() != null ? request.getMessage() : getDefaultMessage());
            reminder.setTimezone(request.getTimezone() != null ? request.getTimezone() : "UTC");
            
            NotificationReminder saved = notificationReminderRepository.save(reminder);
            return new NotificationReminderResponse(saved);
        } else {
            // Create new reminder
            NotificationReminder newReminder = new NotificationReminder(
                user,
                request.getReminderTime(),
                request.getFrequency(),
                request.getMessage() != null ? request.getMessage() : getDefaultMessage(),
                request.getTimezone() != null ? request.getTimezone() : "UTC"
            );
            
            NotificationReminder saved = notificationReminderRepository.save(newReminder);
            return new NotificationReminderResponse(saved);
        }
    }
    
    /**
     * Get all reminders for a user
     */
    public List<NotificationReminderResponse> getUserReminders(User user) {
        List<NotificationReminder> reminders = notificationReminderRepository.findByUser(user);
        return reminders.stream()
                .map(NotificationReminderResponse::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Get active reminders for a user
     */
    public List<NotificationReminderResponse> getActiveUserReminders(User user) {
        List<NotificationReminder> reminders = notificationReminderRepository.findByUserAndIsActiveTrue(user);
        return reminders.stream()
                .map(NotificationReminderResponse::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Update reminder status (activate/deactivate)
     */
    public NotificationReminderResponse updateReminderStatus(User user, Long reminderId, boolean isActive) {
        Optional<NotificationReminder> reminderOpt = notificationReminderRepository.findById(reminderId);
        
        if (reminderOpt.isPresent()) {
            NotificationReminder reminder = reminderOpt.get();
            
            // Check if reminder belongs to the user
            if (!reminder.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Reminder does not belong to the user");
            }
            
            reminder.setIsActive(isActive);
            NotificationReminder saved = notificationReminderRepository.save(reminder);
            return new NotificationReminderResponse(saved);
        } else {
            throw new RuntimeException("Reminder not found");
        }
    }
    
    /**
     * Delete a reminder
     */
    public void deleteReminder(User user, Long reminderId) {
        Optional<NotificationReminder> reminderOpt = notificationReminderRepository.findById(reminderId);
        
        if (reminderOpt.isPresent()) {
            NotificationReminder reminder = reminderOpt.get();
            
            // Check if reminder belongs to the user
            if (!reminder.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Reminder does not belong to the user");
            }
            
            notificationReminderRepository.delete(reminder);
        } else {
            throw new RuntimeException("Reminder not found");
        }
    }
    
    /**
     * Process reminders that should be sent now
     * This method is called by the scheduler
     */
    public void processReminders() {
        LocalTime currentTime = LocalTime.now();
        LocalTime startTime = currentTime.minusMinutes(1);
        LocalTime endTime = currentTime.plusMinutes(1);
        
        List<NotificationReminder> remindersToSend = notificationReminderRepository
            .findActiveRemindersByTimeRange(startTime, endTime);
        
        for (NotificationReminder reminder : remindersToSend) {
            if (shouldSendReminder(reminder)) {
                sendReminder(reminder);
                updateLastSentTime(reminder);
            }
        }
    }
    
    /**
     * Check if a reminder should be sent based on frequency and last sent time
     */
    private boolean shouldSendReminder(NotificationReminder reminder) {
        if (reminder.getLastSentAt() == null) {
            return true; // First time sending
        }
        
        LocalDateTime lastSent = reminder.getLastSentAt();
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek currentDay = now.getDayOfWeek();
        
        switch (reminder.getFrequency()) {
            case DAILY:
                return lastSent.toLocalDate().isBefore(now.toLocalDate());
            
            case WEEKLY:
                return lastSent.isBefore(now.minusWeeks(1));
            
            case WEEKDAYS:
                return lastSent.toLocalDate().isBefore(now.toLocalDate()) && 
                       currentDay != DayOfWeek.SATURDAY && currentDay != DayOfWeek.SUNDAY;
            
            case WEEKENDS:
                return lastSent.toLocalDate().isBefore(now.toLocalDate()) && 
                       (currentDay == DayOfWeek.SATURDAY || currentDay == DayOfWeek.SUNDAY);
            
            case CUSTOM:
                return lastSent.toLocalDate().isBefore(now.toLocalDate());
            
            default:
                return false;
        }
    }
    
    /**
     * Send the reminder notification
     * In a real implementation, this would send an email, push notification, etc.
     */
    private void sendReminder(NotificationReminder reminder) {
        // Log the reminder (in real implementation, send email/push notification)
        System.out.println("Sending reminder to user: " + reminder.getUser().getUsername());
        System.out.println("Message: " + reminder.getMessage());
        System.out.println("Time: " + reminder.getReminderTime());
        System.out.println("Frequency: " + reminder.getFrequency().getDisplayName());
        
        // Here you would integrate with:
        // - Email service (SendGrid, SES, etc.)
        // - Push notification service (Firebase, APNs, etc.)
        // - SMS service (Twilio, etc.)
    }
    
    /**
     * Update the last sent time for a reminder
     */
    private void updateLastSentTime(NotificationReminder reminder) {
        reminder.setLastSentAt(LocalDateTime.now());
        notificationReminderRepository.save(reminder);
    }
    
    /**
     * Get default reminder message
     */
    private String getDefaultMessage() {
        return "Time for your daily mood check-in! How are you feeling today?";
    }
    
    /**
     * Get reminders that need to be processed considering timezone
     */
    public List<NotificationReminder> getRemindersToProcess(LocalTime currentTime) {
        LocalTime startTime = currentTime.minusMinutes(1);
        LocalTime endTime = currentTime.plusMinutes(1);
        
        return notificationReminderRepository.findActiveRemindersByTimeRange(startTime, endTime);
    }
    
    /**
     * Convert time to user's timezone
     */
    private LocalTime convertToUserTimezone(LocalTime utcTime, String userTimezone) {
        try {
            ZonedDateTime utcDateTime = ZonedDateTime.now(ZoneId.of("UTC"))
                .with(utcTime);
            ZonedDateTime userDateTime = utcDateTime.withZoneSameInstant(ZoneId.of(userTimezone));
            return userDateTime.toLocalTime();
        } catch (Exception e) {
            // If timezone conversion fails, return UTC time
            return utcTime;
        }
    }
}