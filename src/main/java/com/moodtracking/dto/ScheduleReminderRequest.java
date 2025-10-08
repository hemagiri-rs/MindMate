package com.moodtracking.dto;

import java.time.LocalTime;

import com.moodtracking.models.NotificationReminder;

public class ScheduleReminderRequest {
    private LocalTime reminderTime;
    private NotificationReminder.ReminderFrequency frequency;
    private String message;
    private String timezone;
    
    // Constructors
    public ScheduleReminderRequest() {}
    
    public ScheduleReminderRequest(LocalTime reminderTime, NotificationReminder.ReminderFrequency frequency, String message, String timezone) {
        this.reminderTime = reminderTime;
        this.frequency = frequency;
        this.message = message;
        this.timezone = timezone;
    }
    
    // Getters and Setters
    public LocalTime getReminderTime() { return reminderTime; }
    public void setReminderTime(LocalTime reminderTime) { this.reminderTime = reminderTime; }
    
    public NotificationReminder.ReminderFrequency getFrequency() { return frequency; }
    public void setFrequency(NotificationReminder.ReminderFrequency frequency) { this.frequency = frequency; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
}