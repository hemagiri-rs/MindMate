package com.moodtracking.dto;

public class SongRecommendation {
    
    private String title;
    private String artist;
    private String album;
    private String genre;
    private Integer duration; // in seconds
    private String spotifyId; // for future Spotify integration
    private String previewUrl;
    
    // Constructors
    public SongRecommendation() {}
    
    public SongRecommendation(String title, String artist, String album, String genre) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.genre = genre;
    }
    
    public SongRecommendation(String title, String artist, String album, String genre, 
                            Integer duration, String spotifyId, String previewUrl) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.genre = genre;
        this.duration = duration;
        this.spotifyId = spotifyId;
        this.previewUrl = previewUrl;
    }
    
    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }
    
    public String getAlbum() { return album; }
    public void setAlbum(String album) { this.album = album; }
    
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    
    public String getSpotifyId() { return spotifyId; }
    public void setSpotifyId(String spotifyId) { this.spotifyId = spotifyId; }
    
    public String getPreviewUrl() { return previewUrl; }
    public void setPreviewUrl(String previewUrl) { this.previewUrl = previewUrl; }
}