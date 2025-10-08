package com.moodtracking.dto;

import java.util.List;

public class MovieRecommendation {
    
    private String title;
    private String director;
    private Integer year;
    private String genre;
    private String plot;
    private Double rating; // IMDb rating
    private Integer duration; // in minutes
    private String tmdbId; // for future TMDB integration
    private String posterUrl;
    private List<String> cast;
    
    // Constructors
    public MovieRecommendation() {}
    
    public MovieRecommendation(String title, String director, Integer year, String genre, String plot) {
        this.title = title;
        this.director = director;
        this.year = year;
        this.genre = genre;
        this.plot = plot;
    }
    
    public MovieRecommendation(String title, String director, Integer year, String genre, 
                             String plot, Double rating, Integer duration, 
                             String tmdbId, String posterUrl, List<String> cast) {
        this.title = title;
        this.director = director;
        this.year = year;
        this.genre = genre;
        this.plot = plot;
        this.rating = rating;
        this.duration = duration;
        this.tmdbId = tmdbId;
        this.posterUrl = posterUrl;
        this.cast = cast;
    }
    
    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }
    
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    
    public String getPlot() { return plot; }
    public void setPlot(String plot) { this.plot = plot; }
    
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    
    public String getTmdbId() { return tmdbId; }
    public void setTmdbId(String tmdbId) { this.tmdbId = tmdbId; }
    
    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    
    public List<String> getCast() { return cast; }
    public void setCast(List<String> cast) { this.cast = cast; }
}