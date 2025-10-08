package com.moodtracking.controllers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
@CrossOrigin(origins = "*", maxAge = 3600)
public class HealthController {
    
    @GetMapping("/status")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "Mood Tracking System");
        response.put("version", "1.0.0");
        
        return ResponseEntity.ok(response);
    }
    
    // Alias for /check (backward compatibility)
    @GetMapping("/check")
    public ResponseEntity<?> healthCheckAlias() {
        return healthCheck();
    }
}