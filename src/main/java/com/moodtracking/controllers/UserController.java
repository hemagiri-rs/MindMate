package com.moodtracking.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moodtracking.dto.PasswordChangeRequest;
import com.moodtracking.dto.UserProfileRequest;
import com.moodtracking.dto.UserProfileResponse;
import com.moodtracking.models.User;
import com.moodtracking.services.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            UserProfileResponse profile = userService.getUserProfile(user);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error retrieving user profile: " + e.getMessage());
        }
    }
    
    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@Valid @RequestBody UserProfileRequest request,
                                             Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            User updatedUser = userService.updateUserProfile(user, request);
            UserProfileResponse profile = userService.getUserProfile(updatedUser);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body("Error updating user profile: " + e.getMessage());
        }
    }
    
    @PutMapping("/profile/password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody PasswordChangeRequest request,
                                          Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            userService.changePassword(user, request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok("Password changed successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body("Error changing password: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/profile")
    public ResponseEntity<?> deleteUserAccount(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            userService.deleteUser(user);
            return ResponseEntity.ok("User account deleted successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error deleting user account: " + e.getMessage());
        }
    }
}