package com.neuroguard.monitoringservice.dto;

import java.time.LocalDate;
import java.util.List;

public class NutritionDTO {
    private String patientId;
    private LocalDate date;
    private int dailyCalories;
    private int targetCalories;
    private int hydrationPercent;
    private int protein;
    private int carbs;
    private int fats;
    private List<MealDTO> meals;

    public NutritionDTO() {}

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public int getDailyCalories() { return dailyCalories; }
    public void setDailyCalories(int dailyCalories) { this.dailyCalories = dailyCalories; }

    public int getTargetCalories() { return targetCalories; }
    public void setTargetCalories(int targetCalories) { this.targetCalories = targetCalories; }

    public int getHydrationPercent() { return hydrationPercent; }
    public void setHydrationPercent(int hydrationPercent) { this.hydrationPercent = hydrationPercent; }

    public int getProtein() { return protein; }
    public void setProtein(int protein) { this.protein = protein; }

    public int getCarbs() { return carbs; }
    public void setCarbs(int carbs) { this.carbs = carbs; }

    public int getFats() { return fats; }
    public void setFats(int fats) { this.fats = fats; }

    public List<MealDTO> getMeals() { return meals; }
    public void setMeals(List<MealDTO> meals) { this.meals = meals; }
}
