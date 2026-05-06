package com.neuroguard.monitoringservice.controller;

import com.neuroguard.monitoringservice.dto.MealDTO;
import com.neuroguard.monitoringservice.dto.NutritionDTO;
import com.neuroguard.monitoringservice.service.NutritionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;


@RestController
// CRITICAL FIX: context-path in application.properties is already "/api/monitoring",
// so this mapping must be ONLY "/nutrition" — not the full path.
// Wrong path was: /api/monitoring/api/monitoring/nutrition → 404 every request.
// Correct path is: /api/monitoring/nutrition → matches frontend environment.monitoringApi + "/nutrition"
@RequestMapping("/nutrition")
public class NutritionController {

    private final NutritionService nutritionService;

    public NutritionController(NutritionService nutritionService) {
        this.nutritionService = nutritionService;
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<NutritionDTO> getNutritionToday(@PathVariable String patientId) {
        NutritionDTO dto = nutritionService.getDailyNutrition(patientId, LocalDate.now());
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{patientId}/meals")
    public ResponseEntity<NutritionDTO> addMeal(@PathVariable String patientId, @RequestBody MealDTO mealDTO) {
        NutritionDTO dto = nutritionService.addMeal(patientId, mealDTO);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/search")
    public ResponseEntity<String> searchFood(@RequestParam String query) {
        String result = nutritionService.searchFoodFromApi(query);
        return ResponseEntity.ok().header("Content-Type", "application/json").body(result);
    }

    @DeleteMapping("/{patientId}/meals/{mealId}")
    public ResponseEntity<NutritionDTO> deleteMeal(@PathVariable String patientId, @PathVariable String mealId) {
        NutritionDTO dto = nutritionService.deleteMeal(patientId, mealId);
        return ResponseEntity.ok(dto);
    }
}
