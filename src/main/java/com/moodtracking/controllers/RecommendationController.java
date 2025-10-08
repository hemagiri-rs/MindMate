package com.moodtracking.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.moodtracking.dto.MovieRecommendation;
import com.moodtracking.dto.RecommendationResponse;
import com.moodtracking.dto.SongRecommendation;
import com.moodtracking.models.Mood.MoodType;
import com.moodtracking.models.User;
import com.moodtracking.services.RecommendationService;

@RestController
@RequestMapping("/recommend")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RecommendationController {
    
    @Autowired
    private RecommendationService recommendationService;
    
    @GetMapping("/songs")
    public ResponseEntity<?> getSongRecommendations(@RequestParam(required = false) MoodType moodType,
                                                  Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            List<SongRecommendation> songs;
            MoodType basedOnMood;
            String message;
            
            if (moodType != null) {
                // Get recommendations for specific mood type
                songs = recommendationService.getSongRecommendations(moodType);
                basedOnMood = moodType;
                message = "Song recommendations based on " + moodType.getDisplayName().toLowerCase() + " mood";
            } else {
                // Get recommendations based on user's recent mood
                songs = recommendationService.getSongRecommendationsForUser(user);
                basedOnMood = recommendationService.getMostRecentMoodType(user);
                
                if (basedOnMood != null) {
                    message = "Song recommendations based on your recent " + basedOnMood.getDisplayName().toLowerCase() + " mood";
                } else {
                    message = "General song recommendations - track your mood for personalized suggestions!";
                }
            }
            
            RecommendationResponse response = new RecommendationResponse(basedOnMood, message);
            response.setSongs(songs);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error getting song recommendations: " + e.getMessage());
        }
    }
    
    @GetMapping("/movies")
    public ResponseEntity<?> getMovieRecommendations(@RequestParam(required = false) MoodType moodType,
                                                   Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            List<MovieRecommendation> movies;
            MoodType basedOnMood;
            String message;
            
            if (moodType != null) {
                // Get recommendations for specific mood type
                movies = recommendationService.getMovieRecommendations(moodType);
                basedOnMood = moodType;
                message = "Movie recommendations based on " + moodType.getDisplayName().toLowerCase() + " mood";
            } else {
                // Get recommendations based on user's recent mood
                movies = recommendationService.getMovieRecommendationsForUser(user);
                basedOnMood = recommendationService.getMostRecentMoodType(user);
                
                if (basedOnMood != null) {
                    message = "Movie recommendations based on your recent " + basedOnMood.getDisplayName().toLowerCase() + " mood";
                } else {
                    message = "General movie recommendations - track your mood for personalized suggestions!";
                }
            }
            
            RecommendationResponse response = new RecommendationResponse(basedOnMood, message);
            response.setMovies(movies);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error getting movie recommendations: " + e.getMessage());
        }
    }
    
    @GetMapping("/all")
    public ResponseEntity<?> getAllRecommendations(@RequestParam(required = false) MoodType moodType,
                                                 Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            List<SongRecommendation> songs;
            List<MovieRecommendation> movies;
            MoodType basedOnMood;
            String message;
            
            if (moodType != null) {
                // Get recommendations for specific mood type
                songs = recommendationService.getSongRecommendations(moodType);
                movies = recommendationService.getMovieRecommendations(moodType);
                basedOnMood = moodType;
                message = "Recommendations based on " + moodType.getDisplayName().toLowerCase() + " mood";
            } else {
                // Get recommendations based on user's recent mood
                songs = recommendationService.getSongRecommendationsForUser(user);
                movies = recommendationService.getMovieRecommendationsForUser(user);
                basedOnMood = recommendationService.getMostRecentMoodType(user);
                
                if (basedOnMood != null) {
                    message = "Recommendations based on your recent " + basedOnMood.getDisplayName().toLowerCase() + " mood";
                } else {
                    message = "General recommendations - track your mood for personalized suggestions!";
                }
            }
            
            RecommendationResponse response = new RecommendationResponse(basedOnMood, message, songs, movies);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error getting recommendations: " + e.getMessage());
        }
    }
    
    @GetMapping("/moods")
    public ResponseEntity<?> getAvailableMoods() {
        try {
            MoodType[] moods = MoodType.values();
            return ResponseEntity.ok(moods);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error getting available moods: " + e.getMessage());
        }
    }
}