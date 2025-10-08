package com.moodtracking.exceptions;

public class MoodNotFoundException extends RuntimeException {
    public MoodNotFoundException(String message) {
        super(message);
    }
    
    public MoodNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}