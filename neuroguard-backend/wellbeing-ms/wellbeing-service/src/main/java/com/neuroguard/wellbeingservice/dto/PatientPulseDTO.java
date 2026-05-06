package com.neuroguard.wellbeingservice.dto;

public class PatientPulseDTO {
    private String moodValue; // Latest mood + emoji
    private String sleepValue; // "7.5h (Good)"
    private String hydrationValue; // "75%"
    private String status; // "stable", "monitor", "attention"
    private String sleepQuality;

    public String getSleepQuality() {
        return sleepQuality;
    }

    public void setSleepQuality(String sleepQuality) {
        this.sleepQuality = sleepQuality;
    }

    public PatientPulseDTO() {
    }

    public PatientPulseDTO(String moodValue, String sleepValue, String hydrationValue, String status, String sleepQuality) {
        this.moodValue = moodValue;
        this.sleepValue = sleepValue;
        this.hydrationValue = hydrationValue;
        this.status = status;
        this.sleepQuality = sleepQuality;
    }

    public String getMoodValue() {
        return moodValue;
    }

    public void setMoodValue(String moodValue) {
        this.moodValue = moodValue;
    }

    public String getSleepValue() {
        return sleepValue;
    }

    public void setSleepValue(String sleepValue) {
        this.sleepValue = sleepValue;
    }

    public String getHydrationValue() {
        return hydrationValue;
    }

    public void setHydrationValue(String hydrationValue) {
        this.hydrationValue = hydrationValue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
