package com.neuroguard.wellbeingservice.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "sleep_logs")
public class Sleep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private Double hours;

    @Column(nullable = false)
    private String quality;

    @Column(nullable = false)
    private LocalDate date;

    private Integer disturbances;

    public Sleep() {
    }

    public Sleep(Long id, String userId, Double hours, String quality, LocalDate date, Integer disturbances) {
        this.id = id;
        this.userId = userId;
        this.hours = hours;
        this.quality = quality;
        this.date = date;
        this.disturbances = disturbances;
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

    public Double getHours() {
        return hours;
    }

    public void setHours(Double hours) {
        this.hours = hours;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getDisturbances() {
        return disturbances;
    }

    public void setDisturbances(Integer disturbances) {
        this.disturbances = disturbances;
    }
}
