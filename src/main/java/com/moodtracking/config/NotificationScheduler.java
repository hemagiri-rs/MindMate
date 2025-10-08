package com.moodtracking.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.moodtracking.services.NotificationService;

@Component
public class NotificationScheduler {
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Process reminders every minute
     * This checks for any reminders that should be sent in the current time window
     */
    @Scheduled(fixedRate = 60000) // Run every 60 seconds (1 minute)
    public void processReminders() {
        try {
            notificationService.processReminders();
        } catch (Exception e) {
            System.err.println("Error processing reminders: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Alternative: Process reminders using cron expression
     * This example runs every minute, but you can adjust as needed
     * 
     * @Scheduled(cron = "0 * * * * *") // Every minute at second 0
     * public void processRemindersCron() {
     *     processReminders();
     * }
     */
    
    /**
     * Cleanup old reminders daily at 2 AM
     * Remove inactive reminders older than 30 days
     */
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2:00 AM
    public void cleanupOldReminders() {
        try {
            // This could be implemented in NotificationService
            System.out.println("Running daily cleanup of old reminders...");
            // notificationService.cleanupOldReminders();
        } catch (Exception e) {
            System.err.println("Error during reminder cleanup: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Health check for scheduler - runs every 10 minutes
     * Logs that the scheduler is running properly
     */
    @Scheduled(fixedRate = 600000) // Run every 10 minutes
    public void schedulerHealthCheck() {
        System.out.println("Notification scheduler is running - " + java.time.LocalDateTime.now());
    }
}