package com.moodtracking.controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.moodtracking.dto.MoodRequest;
import com.moodtracking.dto.MoodResponse;
import com.moodtracking.dto.MoodStatsResponse;
import com.moodtracking.models.User;
import com.moodtracking.services.MoodService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/mood")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MoodController {
    
    @Autowired
    private MoodService moodService;
    
    @PostMapping("/add")
    public ResponseEntity<?> addMood(@Valid @RequestBody MoodRequest request, 
                                   Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            MoodResponse moodResponse = moodService.addMood(request, user);
            return ResponseEntity.ok(moodResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error adding mood entry: " + e.getMessage());
        }
    }
    
    @GetMapping("/history")
    public ResponseEntity<?> getMoodHistory(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            List<MoodResponse> moodHistory = moodService.getMoodHistory(user);
            return ResponseEntity.ok(moodHistory);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error retrieving mood history: " + e.getMessage());
        }
    }
    
    @GetMapping("/history/recent")
    public ResponseEntity<?> getRecentMoods(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            List<MoodResponse> recentMoods = moodService.getRecentMoods(user);
            return ResponseEntity.ok(recentMoods);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error retrieving recent moods: " + e.getMessage());
        }
    }
    
    @GetMapping("/history/range")
    public ResponseEntity<?> getMoodHistoryInRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            List<MoodResponse> moodHistory = moodService.getMoodHistoryInDateRange(user, startDate, endDate);
            return ResponseEntity.ok(moodHistory);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error retrieving mood history: " + e.getMessage());
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<?> getMoodStats(@RequestParam(defaultValue = "30") int days,
                                        Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            MoodStatsResponse stats = moodService.getMoodStats(user, days);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error retrieving mood statistics: " + e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getMoodById(@PathVariable Long id, Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            Optional<MoodResponse> mood = moodService.getMoodById(id, user);
            if (mood.isPresent()) {
                return ResponseEntity.ok(mood.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error retrieving mood: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMood(@PathVariable Long id, 
                                      @Valid @RequestBody MoodRequest request,
                                      Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            Optional<MoodResponse> updatedMood = moodService.updateMood(id, request, user);
            if (updatedMood.isPresent()) {
                return ResponseEntity.ok(updatedMood.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error updating mood: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMood(@PathVariable Long id, Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            boolean deleted = moodService.deleteMood(id, user);
            if (deleted) {
                return ResponseEntity.ok("Mood deleted successfully!");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error deleting mood: " + e.getMessage());
        }
    }
}