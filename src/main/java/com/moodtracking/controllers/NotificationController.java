package com.moodtracking.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moodtracking.dto.NotificationReminderResponse;
import com.moodtracking.dto.ScheduleReminderRequest;
import com.moodtracking.models.User;
import com.moodtracking.services.NotificationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = "*", maxAge = 3600)
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Schedule a new reminder
     */
    @PostMapping("/schedule")
    public ResponseEntity<?> scheduleReminder(@Valid @RequestBody ScheduleReminderRequest request,
                                            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            NotificationReminderResponse response = notificationService.scheduleReminder(user, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse("Error scheduling reminder: " + e.getMessage()));
        }
    }
    
    /**
     * Get all reminders for the authenticated user
     */
    @GetMapping("/reminders")
    public ResponseEntity<?> getUserReminders(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            List<NotificationReminderResponse> reminders = notificationService.getUserReminders(user);
            return ResponseEntity.ok(reminders);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse("Error fetching reminders: " + e.getMessage()));
        }
    }
    
    /**
     * Get active reminders for the authenticated user
     */
    @GetMapping("/reminders/active")
    public ResponseEntity<?> getActiveReminders(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            List<NotificationReminderResponse> reminders = notificationService.getActiveUserReminders(user);
            return ResponseEntity.ok(reminders);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse("Error fetching active reminders: " + e.getMessage()));
        }
    }
    
    /**
     * Update reminder status (activate/deactivate)
     */
    @PatchMapping("/reminders/{id}/status")
    public ResponseEntity<?> updateReminderStatus(@PathVariable Long id,
                                                 @RequestBody Map<String, Boolean> statusUpdate,
                                                 Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            Boolean isActive = statusUpdate.get("isActive");
            
            if (isActive == null) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Missing 'isActive' field in request body"));
            }
            
            NotificationReminderResponse response = notificationService.updateReminderStatus(user, id, isActive);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse("Error updating reminder: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse("Error updating reminder: " + e.getMessage()));
        }
    }
    
    /**
     * Delete a reminder
     */
    @DeleteMapping("/reminders/{id}")
    public ResponseEntity<?> deleteReminder(@PathVariable Long id,
                                          Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            notificationService.deleteReminder(user, id);
            return ResponseEntity.ok(createSuccessResponse("Reminder deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse("Error deleting reminder: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse("Error deleting reminder: " + e.getMessage()));
        }
    }
    
    /**
     * Update an existing reminder
     */
    @PutMapping("/reminders/{id}")
    public ResponseEntity<?> updateReminder(@PathVariable Long id,
                                          @Valid @RequestBody ScheduleReminderRequest request,
                                          Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            
            // First deactivate the old reminder
            notificationService.updateReminderStatus(user, id, false);
            
            // Then create a new one with updated settings
            NotificationReminderResponse response = notificationService.scheduleReminder(user, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse("Error updating reminder: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse("Error updating reminder: " + e.getMessage()));
        }
    }
    
    /**
     * Get reminder statistics for the user
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getReminderStats(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            List<NotificationReminderResponse> allReminders = notificationService.getUserReminders(user);
            List<NotificationReminderResponse> activeReminders = notificationService.getActiveUserReminders(user);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalReminders", allReminders.size());
            stats.put("activeReminders", activeReminders.size());
            stats.put("inactiveReminders", allReminders.size() - activeReminders.size());
            
            // Count by frequency
            Map<String, Long> frequencyStats = activeReminders.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    r -> r.getFrequency().name(),
                    java.util.stream.Collectors.counting()
                ));
            stats.put("frequencyBreakdown", frequencyStats);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse("Error fetching reminder stats: " + e.getMessage()));
        }
    }
    
    /**
     * Test endpoint to manually trigger reminder processing (for development)
     */
    @PostMapping("/test/process")
    public ResponseEntity<?> testProcessReminders() {
        try {
            notificationService.processReminders();
            return ResponseEntity.ok(createSuccessResponse("Reminder processing triggered successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse("Error processing reminders: " + e.getMessage()));
        }
    }
    
    // Helper methods for response formatting
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        response.put("status", "error");
        return response;
    }
    
    private Map<String, String> createSuccessResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        response.put("status", "success");
        return response;
    }
}