package com.neuroguard.wellbeingservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cognitive_scores")
public class CognitiveScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String patientId;
    
    @Enumerated(EnumType.STRING)
    private GameType gameType;

    private Integer score;
    private Long timeSpentSeconds;
    private LocalDateTime timestamp;

    public enum GameType {
        MEMORY,
        ORIENTATION,
        WORD_RECALL
    }

    public CognitiveScore() {
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public GameType getGameType() { return gameType; }
    public void setGameType(GameType gameType) { this.gameType = gameType; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Long getTimeSpentSeconds() { return timeSpentSeconds; }
    public void setTimeSpentSeconds(Long timeSpentSeconds) { this.timeSpentSeconds = timeSpentSeconds; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
