package com.moodtracking.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.moodtracking.models.Note;
import com.moodtracking.models.User;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUserOrderByCreatedAtDesc(User user);
    Optional<Note> findByIdAndUser(Long id, User user);
    List<Note> findByUserAndCategoryOrderByCreatedAtDesc(User user, String category);
    void deleteByIdAndUser(Long id, User user);
}
