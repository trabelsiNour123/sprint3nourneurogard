package com.neuroguard.monitoringservice.dto;

import java.util.List;

public class MealDTO {
    private String id;
    private String name;
    private String type; // Breakfast, Lunch, Dinner, Snack
    private boolean logged;
    private int calories;

    public MealDTO() {}

    public MealDTO(String id, String name, String type, boolean logged, int calories) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.logged = logged;
        this.calories = calories;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isLogged() { return logged; }
    public void setLogged(boolean logged) { this.logged = logged; }

    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }
}
