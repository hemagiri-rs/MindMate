package com.moodtracking.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.RestController;

import com.moodtracking.dto.NoteRequest;
import com.moodtracking.dto.NoteResponse;
import com.moodtracking.models.User;
import com.moodtracking.services.NoteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notes")
@CrossOrigin(origins = "*", maxAge = 3600)
public class NoteController {
    
    @Autowired
    private NoteService noteService;
    
    @PostMapping
    public ResponseEntity<?> createNote(@Valid @RequestBody NoteRequest request,
                                       Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            NoteResponse noteResponse = noteService.createNote(request, user);
            return ResponseEntity.ok(noteResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error creating note: " + e.getMessage());
        }
    }
    
    @GetMapping
    public ResponseEntity<?> getAllNotes(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            List<NoteResponse> notes = noteService.getUserNotes(user);
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error retrieving notes: " + e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getNoteById(@PathVariable Long id,
                                        Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            Optional<NoteResponse> note = noteService.getNoteById(id, user);
            
            if (note.isPresent()) {
                return ResponseEntity.ok(note.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error retrieving note: " + e.getMessage());
        }
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<?> getNotesByCategory(@PathVariable String category,
                                               Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            List<NoteResponse> notes = noteService.getNotesByCategory(user, category);
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error retrieving notes: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateNote(@PathVariable Long id,
                                       @Valid @RequestBody NoteRequest request,
                                       Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            Optional<NoteResponse> updatedNote = noteService.updateNote(id, request, user);
            
            if (updatedNote.isPresent()) {
                return ResponseEntity.ok(updatedNote.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error updating note: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNote(@PathVariable Long id,
                                       Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            boolean deleted = noteService.deleteNote(id, user);
            
            if (deleted) {
                return ResponseEntity.ok("Note deleted successfully");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error deleting note: " + e.getMessage());
        }
    }
}
