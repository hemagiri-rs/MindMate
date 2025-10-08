package com.moodtracking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserProfileRequest {
    
    @NotBlank
    @Size(max = 100)
    private String name;
    
    @NotBlank
    @Size(max = 100)
    @Email
    private String email;
    
    private Integer age;
    
    @Size(max = 1000)
    private String preferences;
    
    // Constructors
    public UserProfileRequest() {}
    
    public UserProfileRequest(String name, String email, Integer age, String preferences) {
        this.name = name;
        this.email = email;
        this.age = age;
        this.preferences = preferences;
    }
    
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    
    public String getPreferences() { return preferences; }
    public void setPreferences(String preferences) { this.preferences = preferences; }
}