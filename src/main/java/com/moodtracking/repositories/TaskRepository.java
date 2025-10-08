package com.moodtracking.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.moodtracking.models.Task;
import com.moodtracking.models.User;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserOrderByCreatedAtDesc(User user);
    Optional<Task> findByIdAndUser(Long id, User user);
    List<Task> findByUserAndCompletedOrderByCreatedAtDesc(User user, Boolean completed);
    List<Task> findByUserAndCategoryOrderByCreatedAtDesc(User user, String category);
    void deleteByIdAndUser(Long id, User user);
}
