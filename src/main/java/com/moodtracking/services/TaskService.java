package com.moodtracking.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moodtracking.dto.TaskRequest;
import com.moodtracking.dto.TaskResponse;
import com.moodtracking.models.Task;
import com.moodtracking.models.User;
import com.moodtracking.repositories.TaskRepository;

@Service
public class TaskService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Transactional
    public TaskResponse createTask(TaskRequest request, User user) {
        Task task = new Task(
            request.getTitle(),
            request.getDescription(),
            request.getCategory(),
            request.getPriority(),
            user
        );
        
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        
        if (request.getCompleted() != null) {
            task.setCompleted(request.getCompleted());
        }
        
        Task savedTask = taskRepository.save(task);
        return convertToResponse(savedTask);
    }
    
    public List<TaskResponse> getUserTasks(User user) {
        List<Task> tasks = taskRepository.findByUserOrderByCreatedAtDesc(user);
        return tasks.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public Optional<TaskResponse> getTaskById(Long id, User user) {
        return taskRepository.findByIdAndUser(id, user)
                .map(this::convertToResponse);
    }
    
    public List<TaskResponse> getTasksByStatus(User user, Boolean completed) {
        List<Task> tasks = taskRepository.findByUserAndCompletedOrderByCreatedAtDesc(user, completed);
        return tasks.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public List<TaskResponse> getTasksByCategory(User user, String category) {
        List<Task> tasks = taskRepository.findByUserAndCategoryOrderByCreatedAtDesc(user, category);
        return tasks.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public Optional<TaskResponse> updateTask(Long id, TaskRequest request, User user) {
        Optional<Task> taskOptional = taskRepository.findByIdAndUser(id, user);
        
        if (taskOptional.isPresent()) {
            Task task = taskOptional.get();
            task.setTitle(request.getTitle());
            task.setDescription(request.getDescription());
            task.setCategory(request.getCategory());
            task.setPriority(request.getPriority());
            
            if (request.getCompleted() != null) {
                task.setCompleted(request.getCompleted());
            }
            
            if (request.getDueDate() != null) {
                task.setDueDate(request.getDueDate());
            }
            
            Task updatedTask = taskRepository.save(task);
            return Optional.of(convertToResponse(updatedTask));
        }
        
        return Optional.empty();
    }
    
    @Transactional
    public boolean deleteTask(Long id, User user) {
        Optional<Task> taskOptional = taskRepository.findByIdAndUser(id, user);
        
        if (taskOptional.isPresent()) {
            taskRepository.delete(taskOptional.get());
            return true;
        }
        
        return false;
    }
    
    @Transactional
    public Optional<TaskResponse> toggleTaskCompletion(Long id, User user) {
        Optional<Task> taskOptional = taskRepository.findByIdAndUser(id, user);
        
        if (taskOptional.isPresent()) {
            Task task = taskOptional.get();
            task.setCompleted(!task.getCompleted());
            
            Task updatedTask = taskRepository.save(task);
            return Optional.of(convertToResponse(updatedTask));
        }
        
        return Optional.empty();
    }
    
    private TaskResponse convertToResponse(Task task) {
        return new TaskResponse(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getCategory(),
            task.getPriority(),
            task.getCompleted(),
            task.getDueDate(),
            task.getCreatedAt(),
            task.getUpdatedAt()
        );
    }
}
