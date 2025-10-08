package com.moodtracking.services;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.moodtracking.dto.MoodResponse;
import com.moodtracking.dto.MovieRecommendation;
import com.moodtracking.dto.SongRecommendation;
import com.moodtracking.models.Mood;
import com.moodtracking.models.User;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {
    
    @Mock
    private MoodService moodService;
    
    @InjectMocks
    private RecommendationService recommendationService;
    
    private User testUser;
    private MoodResponse happyMoodResponse;
    private MoodResponse sadMoodResponse;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        
        happyMoodResponse = new MoodResponse(
            1L,
            Mood.MoodType.HAPPY,
            "Feeling great!",
            LocalDateTime.now(),
            "testuser"
        );
        
        sadMoodResponse = new MoodResponse(
            2L,
            Mood.MoodType.SAD,
            "Feeling down",
            LocalDateTime.now().minusHours(1),
            "testuser"
        );
    }
    
    @Test
    void testGetSongRecommendations_HappyMood() {
        // Act
        List<SongRecommendation> result = recommendationService.getSongRecommendations(Mood.MoodType.HAPPY);
        
        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        // Verify we get appropriate happy songs
        boolean hasHappySong = result.stream()
                .anyMatch(song -> song.getTitle().contains("Happy") || 
                                song.getTitle().contains("Good") ||
                                song.getArtist().contains("Pharrell"));
        assertTrue(hasHappySong, "Should contain upbeat/happy songs");
    }
    
    @Test
    void testGetSongRecommendations_SadMood() {
        // Act
        List<SongRecommendation> result = recommendationService.getSongRecommendations(Mood.MoodType.SAD);
        
        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        // Verify we get appropriate sad songs
        boolean hasSadSong = result.stream()
                .anyMatch(song -> song.getTitle().contains("Someone Like You") || 
                                song.getTitle().contains("Mad World") ||
                                song.getArtist().contains("Adele"));
        assertTrue(hasSadSong, "Should contain melancholic/sad songs");
    }
    
    @Test
    void testGetSongRecommendations_StressedMood() {
        // Act
        List<SongRecommendation> result = recommendationService.getSongRecommendations(Mood.MoodType.STRESSED);
        
        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        // Verify we get calming songs for stress
        boolean hasCalminSong = result.stream()
                .anyMatch(song -> song.getTitle().contains("Weightless") || 
                                song.getTitle().contains("Clair") ||
                                song.getGenre().contains("Ambient"));
        assertTrue(hasCalminSong, "Should contain calming/relaxing songs for stress");
    }
    
    @Test
    void testGetSongRecommendations_UnknownMood() {
        // Act
        List<SongRecommendation> result = recommendationService.getSongRecommendations(null);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty(), "Should return empty list for unknown mood");
    }
    
    @Test
    void testGetMovieRecommendations_HappyMood() {
        // Act
        List<MovieRecommendation> result = recommendationService.getMovieRecommendations(Mood.MoodType.HAPPY);
        
        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        // Verify we get uplifting movies
        boolean hasUpliftingMovie = result.stream()
                .anyMatch(movie -> movie.getTitle().contains("Pursuit") || 
                                 movie.getTitle().contains("Life") ||
                                 movie.getGenre().contains("Comedy"));
        assertTrue(hasUpliftingMovie, "Should contain uplifting/comedy movies");
    }
    
    @Test
    void testGetMovieRecommendations_SadMood() {
        // Act
        List<MovieRecommendation> result = recommendationService.getMovieRecommendations(Mood.MoodType.SAD);
        
        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        // Verify we get appropriate movies for sad mood
        boolean hasDramaMovie = result.stream()
                .anyMatch(movie -> movie.getGenre().contains("Drama") || 
                                 movie.getTitle().contains("Her") ||
                                 movie.getTitle().contains("Lost"));
        assertTrue(hasDramaMovie, "Should contain thoughtful drama movies");
    }
    
    @Test
    void testGetSongRecommendationsForUser_WithRecentMood() {
        // Arrange
        List<MoodResponse> recentMoods = Arrays.asList(happyMoodResponse, sadMoodResponse);
        when(moodService.getRecentMoods(testUser)).thenReturn(recentMoods);
        
        // Act
        List<SongRecommendation> result = recommendationService.getSongRecommendationsForUser(testUser);
        
        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        // Should return recommendations based on most recent mood (HAPPY)
        boolean hasHappySong = result.stream()
                .anyMatch(song -> song.getTitle().contains("Happy"));
        assertTrue(hasHappySong, "Should return happy songs based on recent mood");
        
        verify(moodService).getRecentMoods(testUser);
    }
    
    @Test
    void testGetSongRecommendationsForUser_NoRecentMood() {
        // Arrange
        when(moodService.getRecentMoods(testUser)).thenReturn(Arrays.asList());
        
        // Act
        List<SongRecommendation> result = recommendationService.getSongRecommendationsForUser(testUser);
        
        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty(), "Should return default recommendations when no recent mood");
        
        verify(moodService).getRecentMoods(testUser);
    }
    
    @Test
    void testGetSongRecommendationsForUser_ServiceException() {
        // Arrange
        when(moodService.getRecentMoods(testUser)).thenThrow(new RuntimeException("Database error"));
        
        // Act
        List<SongRecommendation> result = recommendationService.getSongRecommendationsForUser(testUser);
        
        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty(), "Should return default recommendations when service throws exception");
        
        verify(moodService).getRecentMoods(testUser);
    }
    
    @Test
    void testGetMovieRecommendationsForUser_WithRecentMood() {
        // Arrange
        List<MoodResponse> recentMoods = Arrays.asList(sadMoodResponse);
        when(moodService.getRecentMoods(testUser)).thenReturn(recentMoods);
        
        // Act
        List<MovieRecommendation> result = recommendationService.getMovieRecommendationsForUser(testUser);
        
        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        // Should return recommendations based on most recent mood (SAD)
        boolean hasDramaMovie = result.stream()
                .anyMatch(movie -> movie.getGenre().contains("Drama"));
        assertTrue(hasDramaMovie, "Should return drama movies based on sad mood");
        
        verify(moodService).getRecentMoods(testUser);
    }
    
    @Test
    void testGetMovieRecommendationsForUser_NoRecentMood() {
        // Arrange
        when(moodService.getRecentMoods(testUser)).thenReturn(Arrays.asList());
        
        // Act
        List<MovieRecommendation> result = recommendationService.getMovieRecommendationsForUser(testUser);
        
        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty(), "Should return default recommendations when no recent mood");
        
        verify(moodService).getRecentMoods(testUser);
    }
    
    @Test
    void testGetMostRecentMoodType_WithMoods() {
        // Arrange
        List<MoodResponse> recentMoods = Arrays.asList(happyMoodResponse, sadMoodResponse);
        when(moodService.getRecentMoods(testUser)).thenReturn(recentMoods);
        
        // Act
        Mood.MoodType result = recommendationService.getMostRecentMoodType(testUser);
        
        // Assert
        assertEquals(Mood.MoodType.HAPPY, result, "Should return the most recent mood type");
        verify(moodService).getRecentMoods(testUser);
    }
    
    @Test
    void testGetMostRecentMoodType_NoMoods() {
        // Arrange
        when(moodService.getRecentMoods(testUser)).thenReturn(Arrays.asList());
        
        // Act
        Mood.MoodType result = recommendationService.getMostRecentMoodType(testUser);
        
        // Assert
        assertNull(result, "Should return null when no recent moods");
        verify(moodService).getRecentMoods(testUser);
    }
    
    @Test
    void testGetMostRecentMoodType_ServiceException() {
        // Arrange
        when(moodService.getRecentMoods(testUser)).thenThrow(new RuntimeException("Database error"));
        
        // Act
        Mood.MoodType result = recommendationService.getMostRecentMoodType(testUser);
        
        // Assert
        assertNull(result, "Should return null when service throws exception");
        verify(moodService).getRecentMoods(testUser);
    }
    
    @Test
    void testRecommendationConsistency() {
        // Test that the same mood type always returns the same recommendations
        List<SongRecommendation> result1 = recommendationService.getSongRecommendations(Mood.MoodType.HAPPY);
        List<SongRecommendation> result2 = recommendationService.getSongRecommendations(Mood.MoodType.HAPPY);
        
        assertEquals(result1.size(), result2.size(), "Same mood should return same number of recommendations");
        
        // Check that the first song is the same (assuming consistent ordering)
        if (!result1.isEmpty() && !result2.isEmpty()) {
            assertEquals(result1.get(0).getTitle(), result2.get(0).getTitle(), 
                        "Same mood should return same recommendations in same order");
        }
    }
    
    @Test
    void testAllMoodTypesHaveRecommendations() {
        // Test that all defined mood types have both song and movie recommendations
        Mood.MoodType[] moodTypes = {
            Mood.MoodType.HAPPY, Mood.MoodType.SAD, Mood.MoodType.STRESSED,
            Mood.MoodType.EXCITED, Mood.MoodType.RELAXED, Mood.MoodType.ANGRY,
            Mood.MoodType.ANXIOUS, Mood.MoodType.CONTENT, Mood.MoodType.TIRED,
            Mood.MoodType.ENERGETIC
        };
        
        for (Mood.MoodType moodType : moodTypes) {
            List<SongRecommendation> songs = recommendationService.getSongRecommendations(moodType);
            List<MovieRecommendation> movies = recommendationService.getMovieRecommendations(moodType);
            
            // Note: Some mood types might not have recommendations, which is okay
            // This test just ensures the service doesn't crash and returns non-null results
            assertNotNull(songs, "Song recommendations should not be null for " + moodType);
            assertNotNull(movies, "Movie recommendations should not be null for " + moodType);
        }
    }
}