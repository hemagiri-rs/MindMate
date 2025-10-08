package com.moodtracking.dto;

import java.util.List;

import com.moodtracking.models.Mood.MoodType;

public class RecommendationResponse {
    
    private MoodType basedOnMood;
    private String message;
    private List<SongRecommendation> songs;
    private List<MovieRecommendation> movies;
    
    // Constructors
    public RecommendationResponse() {}
    
    public RecommendationResponse(MoodType basedOnMood, String message) {
        this.basedOnMood = basedOnMood;
        this.message = message;
    }
    
    public RecommendationResponse(MoodType basedOnMood, String message, 
                                List<SongRecommendation> songs, List<MovieRecommendation> movies) {
        this.basedOnMood = basedOnMood;
        this.message = message;
        this.songs = songs;
        this.movies = movies;
    }
    
    // Getters and Setters
    public MoodType getBasedOnMood() { return basedOnMood; }
    public void setBasedOnMood(MoodType basedOnMood) { this.basedOnMood = basedOnMood; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public List<SongRecommendation> getSongs() { return songs; }
    public void setSongs(List<SongRecommendation> songs) { this.songs = songs; }
    
    public List<MovieRecommendation> getMovies() { return movies; }
    public void setMovies(List<MovieRecommendation> movies) { this.movies = movies; }
}