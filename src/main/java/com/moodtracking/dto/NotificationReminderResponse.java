package com.moodtracking.dto;

import java.time.LocalDateTime;
import java.time.LocalTime;

import com.moodtracking.models.NotificationReminder;

public class NotificationReminderResponse {
    private Long id;
    private LocalTime reminderTime;
    private NotificationReminder.ReminderFrequency frequency;
    private Boolean isActive;
    private String message;
    private String timezone;
    private LocalDateTime createdAt;
    private LocalDateTime lastSentAt;
    
    // Constructors
    public NotificationReminderResponse() {}
    
    public NotificationReminderResponse(NotificationReminder reminder) {
        this.id = reminder.getId();
        this.reminderTime = reminder.getReminderTime();
        this.frequency = reminder.getFrequency();
        this.isActive = reminder.getIsActive();
        this.message = reminder.getMessage();
        this.timezone = reminder.getTimezone();
        this.createdAt = reminder.getCreatedAt();
        this.lastSentAt = reminder.getLastSentAt();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public LocalTime getReminderTime() { return reminderTime; }
    public void setReminderTime(LocalTime reminderTime) { this.reminderTime = reminderTime; }
    
    public NotificationReminder.ReminderFrequency getFrequency() { return frequency; }
    public void setFrequency(NotificationReminder.ReminderFrequency frequency) { this.frequency = frequency; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getLastSentAt() { return lastSentAt; }
    public void setLastSentAt(LocalDateTime lastSentAt) { this.lastSentAt = lastSentAt; }
}