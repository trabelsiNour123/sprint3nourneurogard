package com.neuroguard.monitoringservice.service;

import com.neuroguard.monitoringservice.dto.MealDTO;
import com.neuroguard.monitoringservice.dto.NutritionDTO;
import com.neuroguard.monitoringservice.entity.MealEmbeddable;
import com.neuroguard.monitoringservice.entity.NutritionEntity;
import com.neuroguard.monitoringservice.repository.NutritionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NutritionServiceTest {

    @Mock
    private NutritionRepository nutritionRepository;

    @InjectMocks
    private NutritionService nutritionService;

    private String patientId = "patient-123";
    private LocalDate today = LocalDate.now();
    private NutritionEntity nutritionEntity;

    @BeforeEach
    void setUp() {
        nutritionEntity = new NutritionEntity();
        nutritionEntity.setPatientId(patientId);
        nutritionEntity.setDate(today);
        nutritionEntity.setMeals(new ArrayList<>());
    }

    @Test
    void testAddMeal_ShouldCalculateTotalsCorrectly() {
        // Arrange
        MealDTO mealDTO = new MealDTO(null, "Apple", "SNACK", true, 95);
        when(nutritionRepository.findByPatientIdAndDate(patientId, today)).thenReturn(Optional.of(nutritionEntity));
        when(nutritionRepository.save(any(NutritionEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        NutritionDTO result = nutritionService.addMeal(patientId, mealDTO);

        // Assert
        assertNotNull(result);
        assertEquals(95, result.getDailyCalories());
        // Verify macros (protein 25%, carbs 50%, fats 25%)
        // 95 * 0.25 / 4 = 5.93 -> 6
        assertEquals(6, result.getProtein());
        // 95 * 0.50 / 4 = 11.87 -> 12
        assertEquals(12, result.getCarbs());
        // 95 * 0.25 / 9 = 2.63 -> 3
        assertEquals(3, result.getFats());
        
        verify(nutritionRepository, times(1)).save(any(NutritionEntity.class));
    }

    @Test
    void testDeleteMeal_ShouldRecalculateTotals() {
        // Arrange
        String mealId = "meal-001";
        MealEmbeddable meal = new MealEmbeddable(mealId, "Burger", "LUNCH", true, 500);
        nutritionEntity.getMeals().add(meal);
        nutritionEntity.setDailyCalories(500);

        when(nutritionRepository.findByPatientIdAndDate(patientId, today)).thenReturn(Optional.of(nutritionEntity));
        when(nutritionRepository.save(any(NutritionEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        NutritionDTO result = nutritionService.deleteMeal(patientId, mealId);

        // Assert
        assertEquals(0, result.getDailyCalories());
        assertEquals(0, result.getProtein());
        assertEquals(0, result.getCarbs());
        assertEquals(0, result.getFats());
        assertTrue(nutritionEntity.getMeals().isEmpty());
    }

    @Test
    void testGetDailyNutrition_WhenNotFound_ShouldCreateNew() {
        // Arrange
        when(nutritionRepository.findByPatientIdAndDate(patientId, today)).thenReturn(Optional.empty());
        when(nutritionRepository.findTopByPatientIdOrderByDateDesc(patientId)).thenReturn(Optional.empty());
        when(nutritionRepository.save(any(NutritionEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        NutritionDTO result = nutritionService.getDailyNutrition(patientId, today);

        // Assert
        assertNotNull(result);
        assertEquals(patientId, result.getPatientId());
        assertEquals(0, result.getDailyCalories());
        verify(nutritionRepository, atLeastOnce()).save(any(NutritionEntity.class));
    }
}
