package com.moodtracking.services;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.moodtracking.dto.RegisterRequest;
import com.moodtracking.dto.UserProfileRequest;
import com.moodtracking.dto.UserProfileResponse;
import com.moodtracking.models.User;
import com.moodtracking.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserService userService;
    
    private User testUser;
    private RegisterRequest registerRequest;
    private UserProfileRequest profileRequest;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setAge(25);
        testUser.setPreferences("Music, Movies");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setName("New User");
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setAge(30);
        registerRequest.setPreferences("Books, Sports");
        
        profileRequest = new UserProfileRequest();
        profileRequest.setName("Updated Name");
        profileRequest.setEmail("updated@example.com");
        profileRequest.setAge(26);
        profileRequest.setPreferences("Updated preferences");
    }
    
    @Test
    void testLoadUserByUsername_Success() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        // Act
        UserDetails result = userService.loadUserByUsername("testuser");
        
        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findByUsername("testuser");
    }
    
    @Test
    void testLoadUserByUsername_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        
        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
            UsernameNotFoundException.class,
            () -> userService.loadUserByUsername("nonexistent")
        );
        assertEquals("User not found: nonexistent", exception.getMessage());
        verify(userRepository).findByUsername("nonexistent");
    }
    
    @Test
    void testCreateUser_Success() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // Act
        User result = userService.createUser(registerRequest);
        
        // Assert
        assertNotNull(result);
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("new@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    void testCreateUser_UsernameAlreadyExists() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(true);
        
        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> userService.createUser(registerRequest)
        );
        assertEquals("Username is already taken!", exception.getMessage());
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void testCreateUser_EmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);
        
        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> userService.createUser(registerRequest)
        );
        assertEquals("Email is already in use!", exception.getMessage());
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("new@example.com");
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void testCreateUserSimple_Success() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // Act
        User result = userService.createUser("testuser", "test@example.com", "password");
        
        // Assert
        assertNotNull(result);
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    void testGetUserProfile_Success() {
        // Act
        UserProfileResponse result = userService.getUserProfile(testUser);
        
        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getName(), result.getName());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getAge(), result.getAge());
        assertEquals(testUser.getPreferences(), result.getPreferences());
    }
    
    @Test
    void testUpdateUserProfile_Success() {
        // Arrange
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.save(testUser)).thenReturn(testUser);
        
        // Act
        User result = userService.updateUserProfile(testUser, profileRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals("Updated Name", testUser.getName());
        assertEquals("updated@example.com", testUser.getEmail());
        assertEquals(26, testUser.getAge());
        assertEquals("Updated preferences", testUser.getPreferences());
        verify(userRepository).existsByEmail("updated@example.com");
        verify(userRepository).save(testUser);
    }
    
    @Test
    void testUpdateUserProfile_EmailAlreadyExists() {
        // Arrange
        profileRequest.setEmail("existing@example.com");
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);
        
        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> userService.updateUserProfile(testUser, profileRequest)
        );
        assertEquals("Email is already in use!", exception.getMessage());
        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void testUpdateUserProfile_SameEmail() {
        // Arrange
        profileRequest.setEmail("test@example.com"); // Same as current email
        when(userRepository.save(testUser)).thenReturn(testUser);
        
        // Act
        User result = userService.updateUserProfile(testUser, profileRequest);
        
        // Assert
        assertNotNull(result);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository).save(testUser);
    }
    
    @Test
    void testChangePassword_Success() {
        // Arrange
        when(passwordEncoder.matches("currentPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
        when(userRepository.save(testUser)).thenReturn(testUser);
        
        // Act
        userService.changePassword(testUser, "currentPassword", "newPassword");
        
        // Assert
        verify(passwordEncoder).matches("currentPassword", "encodedPassword");
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(testUser);
        assertEquals("newEncodedPassword", testUser.getPassword());
    }
    
    @Test
    void testChangePassword_IncorrectCurrentPassword() {
        // Arrange
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);
        
        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> userService.changePassword(testUser, "wrongPassword", "newPassword")
        );
        assertEquals("Current password is incorrect!", exception.getMessage());
        verify(passwordEncoder).matches("wrongPassword", "encodedPassword");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void testDeleteUser_Success() {
        // Act
        userService.deleteUser(testUser);
        
        // Assert
        verify(userRepository).delete(testUser);
    }
    
    @Test
    void testFindByUsername_Found() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        // Act
        Optional<User> result = userService.findByUsername("testuser");
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userRepository).findByUsername("testuser");
    }
    
    @Test
    void testFindByUsername_NotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        
        // Act
        Optional<User> result = userService.findByUsername("nonexistent");
        
        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findByUsername("nonexistent");
    }
    
    @Test
    void testFindByEmail_Found() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        
        // Act
        Optional<User> result = userService.findByEmail("test@example.com");
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userRepository).findByEmail("test@example.com");
    }
    
    @Test
    void testExistsByUsername_True() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        
        // Act
        boolean result = userService.existsByUsername("testuser");
        
        // Assert
        assertTrue(result);
        verify(userRepository).existsByUsername("testuser");
    }
    
    @Test
    void testExistsByEmail_False() {
        // Arrange
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        
        // Act
        boolean result = userService.existsByEmail("test@example.com");
        
        // Assert
        assertFalse(result);
        verify(userRepository).existsByEmail("test@example.com");
    }
}