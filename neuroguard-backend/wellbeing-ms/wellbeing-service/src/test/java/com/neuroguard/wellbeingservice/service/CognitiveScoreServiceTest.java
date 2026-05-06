package com.neuroguard.wellbeingservice.service;

import com.neuroguard.wellbeingservice.entity.CognitiveScore;
import com.neuroguard.wellbeingservice.repository.CognitiveScoreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CognitiveScoreServiceTest {

    @Mock
    private CognitiveScoreRepository repository;

    @InjectMocks
    private CognitiveScoreService service;

    @Test
    void testSaveResult_ShouldCallRepositorySave() {
        // Arrange
        CognitiveScore score = new CognitiveScore();
        score.setPatientId("p1");
        score.setGameType(CognitiveScore.GameType.MEMORY);
        score.setScore(85);
        
        when(repository.save(any(CognitiveScore.class))).thenReturn(score);

        // Act
        CognitiveScore saved = service.saveResult(score);

        // Assert
        assertNotNull(saved);
        assertEquals(85, saved.getScore());
        verify(repository, times(1)).save(score);
    }

    @Test
    void testGetPatientResults_ShouldReturnListOfScores() {
        // Arrange
        String patientId = "p1";
        CognitiveScore score1 = new CognitiveScore();
        score1.setPatientId(patientId);
        CognitiveScore score2 = new CognitiveScore();
        score2.setPatientId(patientId);
        
        when(repository.findByPatientIdOrderByTimestampDesc(patientId))
            .thenReturn(Arrays.asList(score1, score2));

        // Act
        List<CognitiveScore> results = service.getPatientResults(patientId);

        // Assert
        assertEquals(2, results.size());
        verify(repository, times(1)).findByPatientIdOrderByTimestampDesc(patientId);
    }

    @Test
    void testGetPatientResultsByGame_ShouldFilterCorrectly() {
        // Arrange
        String patientId = "p1";
        String gameTypeStr = "MEMORY";
        CognitiveScore score = new CognitiveScore();
        score.setPatientId(patientId);
        score.setGameType(CognitiveScore.GameType.MEMORY);
        
        when(repository.findByPatientIdAndGameTypeOrderByTimestampDesc(patientId, CognitiveScore.GameType.MEMORY))
            .thenReturn(Arrays.asList(score));

        // Act
        List<CognitiveScore> results = service.getPatientResultsByGame(patientId, gameTypeStr);

        // Assert
        assertEquals(1, results.size());
        assertEquals(CognitiveScore.GameType.MEMORY, results.get(0).getGameType());
        verify(repository, times(1)).findByPatientIdAndGameTypeOrderByTimestampDesc(patientId, CognitiveScore.GameType.MEMORY);
    }
}
