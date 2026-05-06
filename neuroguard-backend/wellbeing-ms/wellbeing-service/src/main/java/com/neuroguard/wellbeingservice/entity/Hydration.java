package com.neuroguard.wellbeingservice.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "hydration_logs")
public class Hydration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private Integer glassesCount;

    @Column(nullable = false)
    private Integer targetGlasses = 8;

    @Column(nullable = false)
    private LocalDate date;

    public Hydration() {
    }

    public Hydration(Long id, String userId, Integer glassesCount, Integer targetGlasses, LocalDate date) {
        this.id = id;
        this.userId = userId;
        this.glassesCount = glassesCount;
        this.targetGlasses = targetGlasses;
        this.date = date;
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

    public Integer getGlassesCount() {
        return glassesCount;
    }

    public void setGlassesCount(Integer glassesCount) {
        this.glassesCount = glassesCount;
    }

    public Integer getTargetGlasses() {
        return targetGlasses;
    }

    public void setTargetGlasses(Integer targetGlasses) {
        this.targetGlasses = targetGlasses;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
