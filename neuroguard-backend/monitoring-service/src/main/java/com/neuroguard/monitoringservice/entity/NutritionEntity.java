package com.neuroguard.monitoringservice.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "nutrition")
public class NutritionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String patientId;

    private LocalDate date;

    private int dailyCalories;
    private int targetCalories;
    private int hydrationPercent;
    
    private int protein;
    private int carbs;
    private int fats;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "nutrition_meals", joinColumns = @JoinColumn(name = "nutrition_id"))
    private List<MealEmbeddable> meals = new ArrayList<>();

    public NutritionEntity() {
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getDailyCalories() {
        return dailyCalories;
    }

    public void setDailyCalories(int dailyCalories) {
        this.dailyCalories = dailyCalories;
    }

    public int getTargetCalories() {
        return targetCalories;
    }

    public void setTargetCalories(int targetCalories) {
        this.targetCalories = targetCalories;
    }

    public int getHydrationPercent() {
        return hydrationPercent;
    }

    public void setHydrationPercent(int hydrationPercent) {
        this.hydrationPercent = hydrationPercent;
    }

    public int getProtein() {
        return protein;
    }

    public void setProtein(int protein) {
        this.protein = protein;
    }

    public int getCarbs() {
        return carbs;
    }

    public void setCarbs(int carbs) {
        this.carbs = carbs;
    }

    public int getFats() {
        return fats;
    }

    public void setFats(int fats) {
        this.fats = fats;
    }

    public List<MealEmbeddable> getMeals() {
        return meals;
    }

    public void setMeals(List<MealEmbeddable> meals) {
        this.meals = meals;
    }
}
