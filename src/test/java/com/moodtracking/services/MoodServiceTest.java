package com.moodtracking.services;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.moodtracking.dto.MoodRequest;
import com.moodtracking.dto.MoodResponse;
import com.moodtracking.dto.MoodStatsResponse;
import com.moodtracking.models.Mood;
import com.moodtracking.models.User;
import com.moodtracking.repositories.MoodRepository;

@ExtendWith(MockitoExtension.class)
class MoodServiceTest {
    
    @Mock
    private MoodRepository moodRepository;
    
    @InjectMocks
    private MoodService moodService;
    
    private User testUser;
    private User otherUser;
    private Mood testMood;
    private MoodRequest moodRequest;
    private LocalDateTime testTime;
    
    @BeforeEach
    void setUp() {
        testTime = LocalDateTime.now();
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        
        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otheruser");
        otherUser.setName("Other User");
        otherUser.setEmail("other@example.com");
        
        testMood = new Mood();
        testMood.setId(1L);
        testMood.setMoodType(Mood.MoodType.HAPPY);
        testMood.setNotes("Feeling great today!");
        testMood.setTimestamp(testTime);
        testMood.setUser(testUser);
        
        moodRequest = new MoodRequest();
        moodRequest.setMoodType(Mood.MoodType.HAPPY);
        moodRequest.setNotes("Test mood entry");
    }
    
    @Test
    void testAddMood_Success() {
        // Arrange
        when(moodRepository.save(any(Mood.class))).thenReturn(testMood);
        
        // Act
        MoodResponse result = moodService.addMood(moodRequest, testUser);
        
        // Assert
        assertNotNull(result);
        assertEquals(testMood.getId(), result.getId());
        assertEquals(testMood.getMoodType(), result.getMoodType());
        assertEquals(testMood.getNotes(), result.getNotes());
        assertEquals(testMood.getTimestamp(), result.getTimestamp());
        assertEquals(testMood.getUser().getUsername(), result.getUsername());
        
        verify(moodRepository).save(any(Mood.class));
    }
    
    @Test
    void testGetMoodHistory_Success() {
        // Arrange
        Mood mood1 = new Mood(Mood.MoodType.HAPPY, "Happy mood", testUser);
        mood1.setId(1L);
        mood1.setTimestamp(testTime);
        
        Mood mood2 = new Mood(Mood.MoodType.SAD, "Sad mood", testUser);
        mood2.setId(2L);
        mood2.setTimestamp(testTime.minusHours(1));
        
        List<Mood> moods = Arrays.asList(mood1, mood2);
        when(moodRepository.findByUserOrderByTimestampDesc(testUser)).thenReturn(moods);
        
        // Act
        List<MoodResponse> result = moodService.getMoodHistory(testUser);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(mood1.getMoodType(), result.get(0).getMoodType());
        assertEquals(mood2.getMoodType(), result.get(1).getMoodType());
        
        verify(moodRepository).findByUserOrderByTimestampDesc(testUser);
    }
    
    @Test
    void testGetMoodHistory_EmptyList() {
        // Arrange
        when(moodRepository.findByUserOrderByTimestampDesc(testUser)).thenReturn(Arrays.asList());
        
        // Act
        List<MoodResponse> result = moodService.getMoodHistory(testUser);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(moodRepository).findByUserOrderByTimestampDesc(testUser);
    }
    
    @Test
    void testGetMoodHistoryInDateRange_Success() {
        // Arrange
        LocalDateTime startDate = testTime.minusDays(7);
        LocalDateTime endDate = testTime;
        
        Mood mood1 = new Mood(Mood.MoodType.HAPPY, "Happy mood", testUser);
        mood1.setId(1L);
        mood1.setTimestamp(testTime.minusDays(1));
        
        List<Mood> moods = Arrays.asList(mood1);
        when(moodRepository.findByUserAndTimestampBetweenOrderByTimestampDesc(testUser, startDate, endDate))
                .thenReturn(moods);
        
        // Act
        List<MoodResponse> result = moodService.getMoodHistoryInDateRange(testUser, startDate, endDate);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mood1.getMoodType(), result.get(0).getMoodType());
        
        verify(moodRepository).findByUserAndTimestampBetweenOrderByTimestampDesc(testUser, startDate, endDate);
    }
    
    @Test
    void testGetRecentMoods_Success() {
        // Arrange
        Mood mood1 = new Mood(Mood.MoodType.EXCITED, "Recent mood", testUser);
        mood1.setId(1L);
        mood1.setTimestamp(testTime);
        
        List<Mood> moods = Arrays.asList(mood1);
        when(moodRepository.findTop10ByUserOrderByTimestampDesc(testUser)).thenReturn(moods);
        
        // Act
        List<MoodResponse> result = moodService.getRecentMoods(testUser);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mood1.getMoodType(), result.get(0).getMoodType());
        
        verify(moodRepository).findTop10ByUserOrderByTimestampDesc(testUser);
    }
    
    @Test
    void testGetMoodStats_Success() {
        // Arrange
        int days = 30;
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        Long totalMoods = 15L;
        
        Object[] stat1 = {Mood.MoodType.HAPPY, 10L};
        Object[] stat2 = {Mood.MoodType.SAD, 5L};
        List<Object[]> moodStats = Arrays.asList(stat1, stat2);
        
        when(moodRepository.countMoodsByUserAndDateAfter(eq(testUser), any(LocalDateTime.class)))
                .thenReturn(totalMoods);
        when(moodRepository.getMoodStatsByUser(eq(testUser), any(LocalDateTime.class)))
                .thenReturn(moodStats);
        
        // Act
        MoodStatsResponse result = moodService.getMoodStats(testUser, days);
        
        // Assert
        assertNotNull(result);
        assertEquals(totalMoods, result.getTotalMoods());
        assertEquals(Mood.MoodType.HAPPY, result.getMostFrequentMood());
        assertEquals(Integer.valueOf(days), result.getDaysTracked());
        assertEquals(2, result.getMoodCounts().size());
        assertEquals(10L, result.getMoodCounts().get(Mood.MoodType.HAPPY));
        assertEquals(5L, result.getMoodCounts().get(Mood.MoodType.SAD));
        
        verify(moodRepository).countMoodsByUserAndDateAfter(eq(testUser), any(LocalDateTime.class));
        verify(moodRepository).getMoodStatsByUser(eq(testUser), any(LocalDateTime.class));
    }
    
    @Test
    void testGetMoodStats_NoMoods() {
        // Arrange
        int days = 7;
        Long totalMoods = 0L;
        List<Object[]> moodStats = Arrays.asList();
        
        when(moodRepository.countMoodsByUserAndDateAfter(eq(testUser), any(LocalDateTime.class)))
                .thenReturn(totalMoods);
        when(moodRepository.getMoodStatsByUser(eq(testUser), any(LocalDateTime.class)))
                .thenReturn(moodStats);
        
        // Act
        MoodStatsResponse result = moodService.getMoodStats(testUser, days);
        
        // Assert
        assertNotNull(result);
        assertEquals(totalMoods, result.getTotalMoods());
        assertNull(result.getMostFrequentMood());
        assertTrue(result.getMoodCounts().isEmpty());
    }
    
    @Test
    void testGetMoodById_Success() {
        // Arrange
        when(moodRepository.findById(1L)).thenReturn(Optional.of(testMood));
        
        // Act
        Optional<MoodResponse> result = moodService.getMoodById(1L, testUser);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(testMood.getId(), result.get().getId());
        assertEquals(testMood.getMoodType(), result.get().getMoodType());
        
        verify(moodRepository).findById(1L);
    }
    
    @Test
    void testGetMoodById_NotFound() {
        // Arrange
        when(moodRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act
        Optional<MoodResponse> result = moodService.getMoodById(999L, testUser);
        
        // Assert
        assertFalse(result.isPresent());
        
        verify(moodRepository).findById(999L);
    }
    
    @Test
    void testGetMoodById_WrongUser() {
        // Arrange
        when(moodRepository.findById(1L)).thenReturn(Optional.of(testMood));
        
        // Act
        Optional<MoodResponse> result = moodService.getMoodById(1L, otherUser);
        
        // Assert
        assertFalse(result.isPresent());
        
        verify(moodRepository).findById(1L);
    }
    
    @Test
    void testDeleteMood_Success() {
        // Arrange
        when(moodRepository.findById(1L)).thenReturn(Optional.of(testMood));
        doNothing().when(moodRepository).delete(testMood);
        
        // Act
        boolean result = moodService.deleteMood(1L, testUser);
        
        // Assert
        assertTrue(result);
        
        verify(moodRepository).findById(1L);
        verify(moodRepository).delete(testMood);
    }
    
    @Test
    void testDeleteMood_NotFound() {
        // Arrange
        when(moodRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act
        boolean result = moodService.deleteMood(999L, testUser);
        
        // Assert
        assertFalse(result);
        
        verify(moodRepository).findById(999L);
        verify(moodRepository, never()).delete(any(Mood.class));
    }
    
    @Test
    void testDeleteMood_WrongUser() {
        // Arrange
        when(moodRepository.findById(1L)).thenReturn(Optional.of(testMood));
        
        // Act
        boolean result = moodService.deleteMood(1L, otherUser);
        
        // Assert
        assertFalse(result);
        
        verify(moodRepository).findById(1L);
        verify(moodRepository, never()).delete(any(Mood.class));
    }
    
    @Test
    void testUpdateMood_Success() {
        // Arrange
        MoodRequest updateRequest = new MoodRequest();
        updateRequest.setMoodType(Mood.MoodType.EXCITED);
        updateRequest.setNotes("Updated notes");
        
        Mood updatedMood = new Mood(updateRequest.getMoodType(), updateRequest.getNotes(), testUser);
        updatedMood.setId(1L);
        updatedMood.setTimestamp(testTime);
        
        when(moodRepository.findById(1L)).thenReturn(Optional.of(testMood));
        when(moodRepository.save(any(Mood.class))).thenReturn(updatedMood);
        
        // Act
        Optional<MoodResponse> result = moodService.updateMood(1L, updateRequest, testUser);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(updateRequest.getMoodType(), result.get().getMoodType());
        assertEquals(updateRequest.getNotes(), result.get().getNotes());
        
        verify(moodRepository).findById(1L);
        verify(moodRepository).save(any(Mood.class));
    }
    
    @Test
    void testUpdateMood_NotFound() {
        // Arrange
        when(moodRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act
        Optional<MoodResponse> result = moodService.updateMood(999L, moodRequest, testUser);
        
        // Assert
        assertFalse(result.isPresent());
        
        verify(moodRepository).findById(999L);
        verify(moodRepository, never()).save(any(Mood.class));
    }
    
    @Test
    void testUpdateMood_WrongUser() {
        // Arrange
        when(moodRepository.findById(1L)).thenReturn(Optional.of(testMood));
        
        // Act
        Optional<MoodResponse> result = moodService.updateMood(1L, moodRequest, otherUser);
        
        // Assert
        assertFalse(result.isPresent());
        
        verify(moodRepository).findById(1L);
        verify(moodRepository, never()).save(any(Mood.class));
    }
}