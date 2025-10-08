package com.moodtracking.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moodtracking.dto.NoteRequest;
import com.moodtracking.dto.NoteResponse;
import com.moodtracking.models.Note;
import com.moodtracking.models.User;
import com.moodtracking.repositories.NoteRepository;

@Service
public class NoteService {
    
    @Autowired
    private NoteRepository noteRepository;
    
    @Transactional
    public NoteResponse createNote(NoteRequest request, User user) {
        Note note = new Note(
            request.getTitle(),
            request.getContent(),
            request.getCategory(),
            user
        );
        
        Note savedNote = noteRepository.save(note);
        return convertToResponse(savedNote);
    }
    
    public List<NoteResponse> getUserNotes(User user) {
        List<Note> notes = noteRepository.findByUserOrderByCreatedAtDesc(user);
        return notes.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public Optional<NoteResponse> getNoteById(Long id, User user) {
        return noteRepository.findByIdAndUser(id, user)
                .map(this::convertToResponse);
    }
    
    public List<NoteResponse> getNotesByCategory(User user, String category) {
        List<Note> notes = noteRepository.findByUserAndCategoryOrderByCreatedAtDesc(user, category);
        return notes.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public Optional<NoteResponse> updateNote(Long id, NoteRequest request, User user) {
        Optional<Note> noteOptional = noteRepository.findByIdAndUser(id, user);
        
        if (noteOptional.isPresent()) {
            Note note = noteOptional.get();
            note.setTitle(request.getTitle());
            note.setContent(request.getContent());
            note.setCategory(request.getCategory());
            
            Note updatedNote = noteRepository.save(note);
            return Optional.of(convertToResponse(updatedNote));
        }
        
        return Optional.empty();
    }
    
    @Transactional
    public boolean deleteNote(Long id, User user) {
        Optional<Note> noteOptional = noteRepository.findByIdAndUser(id, user);
        
        if (noteOptional.isPresent()) {
            noteRepository.delete(noteOptional.get());
            return true;
        }
        
        return false;
    }
    
    private NoteResponse convertToResponse(Note note) {
        return new NoteResponse(
            note.getId(),
            note.getTitle(),
            note.getContent(),
            note.getCategory(),
            note.getCreatedAt(),
            note.getUpdatedAt()
        );
    }
}
