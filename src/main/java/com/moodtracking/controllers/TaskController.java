package com.moodtracking.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.moodtracking.dto.TaskRequest;
import com.moodtracking.dto.TaskResponse;
import com.moodtracking.models.User;
import com.moodtracking.services.TaskService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TaskController {
    
    @Autowired
    private TaskService taskService;
    
    @PostMapping
    public ResponseEntity<?> createTask(@Valid @RequestBody TaskRequest request,
                                       Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            TaskResponse taskResponse = taskService.createTask(request, user);
            return ResponseEntity.ok(taskResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error creating task: " + e.getMessage());
        }
    }
    
    @GetMapping
    public ResponseEntity<?> getAllTasks(Authentication authentication,
                                        @RequestParam(required = false) Boolean completed) {
        try {
            User user = (User) authentication.getPrincipal();
            List<TaskResponse> tasks;
            
            if (completed != null) {
                tasks = taskService.getTasksByStatus(user, completed);
            } else {
                tasks = taskService.getUserTasks(user);
            }
            
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error retrieving tasks: " + e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getTaskById(@PathVariable Long id,
                                        Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            Optional<TaskResponse> task = taskService.getTaskById(id, user);
            
            if (task.isPresent()) {
                return ResponseEntity.ok(task.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error retrieving task: " + e.getMessage());
        }
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<?> getTasksByCategory(@PathVariable String category,
                                               Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            List<TaskResponse> tasks = taskService.getTasksByCategory(user, category);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error retrieving tasks: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Long id,
                                       @Valid @RequestBody TaskRequest request,
                                       Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            Optional<TaskResponse> updatedTask = taskService.updateTask(id, request, user);
            
            if (updatedTask.isPresent()) {
                return ResponseEntity.ok(updatedTask.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error updating task: " + e.getMessage());
        }
    }
    
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<?> toggleTaskCompletion(@PathVariable Long id,
                                                  Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            Optional<TaskResponse> updatedTask = taskService.toggleTaskCompletion(id, user);
            
            if (updatedTask.isPresent()) {
                return ResponseEntity.ok(updatedTask.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error toggling task completion: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id,
                                       Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            boolean deleted = taskService.deleteTask(id, user);
            
            if (deleted) {
                return ResponseEntity.ok("Task deleted successfully");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error deleting task: " + e.getMessage());
        }
    }
}
