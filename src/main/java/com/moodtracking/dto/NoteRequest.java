package com.moodtracking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class NoteRequest {
    
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;
    
    private String content;
    
    @NotBlank(message = "Category is required")
    private String category;
    
    // Constructors
    public NoteRequest() {}
    
    public NoteRequest(String title, String content, String category) {
        this.title = title;
        this.content = content;
        this.category = category;
    }
    
    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
