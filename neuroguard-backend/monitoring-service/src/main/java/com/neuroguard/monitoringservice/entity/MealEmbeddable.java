package com.neuroguard.monitoringservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class MealEmbeddable {
    @Column(name = "meal_id")
    private String id;
    private String name;
    private String type; // Breakfast, Lunch, Dinner, Snack
    private boolean logged;
    private int calories;

    public MealEmbeddable() {
    }

    public MealEmbeddable(String id, String name, String type, boolean logged, int calories) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.logged = logged;
        this.calories = calories;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isLogged() {
        return logged;
    }

    public void setLogged(boolean logged) {
        this.logged = logged;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MealEmbeddable that = (MealEmbeddable) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
