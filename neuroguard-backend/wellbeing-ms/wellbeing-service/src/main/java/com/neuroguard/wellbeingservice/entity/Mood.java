package com.neuroguard.wellbeingservice.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "mood_logs", indexes = { @Index(name = "idx_mood_userId", columnList = "userId") })
public class Mood {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String moodLabel;

    @Column(nullable = false)
    private String emoji;

    @CreationTimestamp
    private LocalDateTime timestamp;

    public Mood() {
    }

    public Mood(Long id, String userId, String moodLabel, String emoji, LocalDateTime timestamp) {
        this.id = id;
        this.userId = userId;
        this.moodLabel = moodLabel;
        this.emoji = emoji;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMoodLabel() {
        return moodLabel;
    }

    public void setMoodLabel(String moodLabel) {
        this.moodLabel = moodLabel;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
