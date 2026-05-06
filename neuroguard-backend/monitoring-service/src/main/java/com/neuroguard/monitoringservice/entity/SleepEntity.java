package com.neuroguard.monitoringservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sleep_records")
public class SleepEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String patientId;
    private Double duration;
    private String quality;
    private Integer disturbances;
    private LocalDateTime timestamp;

    public SleepEntity() {
    }

    public SleepEntity(String patientId, Double duration, String quality, Integer disturbances,
            LocalDateTime timestamp) {
        this.patientId = patientId;
        this.duration = duration;
        this.quality = quality;
        this.disturbances = disturbances;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public Integer getDisturbances() {
        return disturbances;
    }

    public void setDisturbances(Integer disturbances) {
        this.disturbances = disturbances;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
