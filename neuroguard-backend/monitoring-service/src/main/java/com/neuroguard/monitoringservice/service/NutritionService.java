package com.neuroguard.monitoringservice.service;

import com.neuroguard.monitoringservice.dto.MealDTO;
import com.neuroguard.monitoringservice.dto.NutritionDTO;
import com.neuroguard.monitoringservice.entity.MealEmbeddable;
import com.neuroguard.monitoringservice.entity.NutritionEntity;
import com.neuroguard.monitoringservice.repository.NutritionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@Service
public class NutritionService {

    private final NutritionRepository nutritionRepository;

    public NutritionService(NutritionRepository nutritionRepository) {
        this.nutritionRepository = nutritionRepository;
    }

    // ... keeping the rest the same ...

    @Transactional
    public NutritionDTO getDailyNutrition(String patientId, LocalDate date) {
        NutritionEntity entity = nutritionRepository.findByPatientIdAndDate(patientId, date)
                .orElseGet(() -> {
                    // return the most recent entry, or seed a fresh one for today
                    return nutritionRepository.findTopByPatientIdOrderByDateDesc(patientId)
                            .orElse(createNewNutrition(patientId, date));
                });

        recalculateTotals(entity);
        return mapToDTO(entity);
    }

    @Transactional
    public NutritionDTO addMeal(String patientId, MealDTO mealDTO) {
        LocalDate today = LocalDate.now();
        NutritionEntity entity = nutritionRepository.findByPatientIdAndDate(patientId, today)
                .orElseGet(() -> createNewNutrition(patientId, today));

        String mealId = UUID.randomUUID().toString();
        MealEmbeddable meal = new MealEmbeddable(mealId, mealDTO.getName(), mealDTO.getType(), mealDTO.isLogged(), mealDTO.getCalories());
        entity.getMeals().add(meal);

        recalculateTotals(entity);

        NutritionEntity saved = nutritionRepository.save(entity);
        return mapToDTO(saved);
    }

    @Transactional
    public NutritionDTO deleteMeal(String patientId, String mealId) {
        LocalDate today = LocalDate.now();
        NutritionEntity entity = nutritionRepository.findByPatientIdAndDate(patientId, today)
                .orElseThrow(() -> new RuntimeException("Nutrition log not found for today"));

        System.out.println("Processing delete request for patient: " + patientId + ", mealId: " + mealId);
        
        boolean removed = entity.getMeals().removeIf(m -> mealId.equals(m.getId()));
        
        if (!removed) {
            System.err.println("Meal NOT found for deletion: " + mealId);
            // Fallback for existing data: check for null ID and match by name/calories? 
            // For now, just throw exception to see what's happening.
            throw new RuntimeException("Meal not found with id: " + mealId);
        }

        // Recalculate totals from scratch to be safe against double-counting or drift
        recalculateTotals(entity);
        
        NutritionEntity saved = nutritionRepository.save(entity);
        return mapToDTO(saved);
    }

    private void recalculateTotals(NutritionEntity entity) {
        int totalCals = 0;
        for (MealEmbeddable m : entity.getMeals()) {
            totalCals += m.getCalories();
        }
        entity.setDailyCalories(totalCals);

        entity.setProtein(Math.round(totalCals * 0.25f / 4));
        entity.setCarbs(Math.round(totalCals * 0.50f / 4));
        entity.setFats(Math.round(totalCals * 0.25f / 9));
    }

    private NutritionEntity createNewNutrition(String patientId, LocalDate date) {
        NutritionEntity entity = new NutritionEntity();
        entity.setPatientId(patientId);
        entity.setDate(date);
        entity.setDailyCalories(0);
        entity.setTargetCalories(2000);
        entity.setHydrationPercent(100);
        entity.setProtein(0);
        entity.setCarbs(0);
        entity.setFats(0);
        entity.setMeals(new ArrayList<>());
        return nutritionRepository.save(entity);
    }

    private NutritionDTO mapToDTO(NutritionEntity entity) {
        NutritionDTO dto = new NutritionDTO();
        dto.setPatientId(entity.getPatientId());
        dto.setDate(entity.getDate());
        dto.setDailyCalories(entity.getDailyCalories());
        dto.setTargetCalories(entity.getTargetCalories());
        dto.setHydrationPercent(entity.getHydrationPercent());
        dto.setProtein(entity.getProtein());
        dto.setCarbs(entity.getCarbs());
        dto.setFats(entity.getFats());
        
        List<MealDTO> meals = entity.getMeals().stream()
                .map(m -> new MealDTO(m.getId(), m.getName(), m.getType(), m.isLogged(), m.getCalories()))
                .collect(Collectors.toList());
        dto.setMeals(meals);
        
        return dto;
    }

    public String searchFoodFromApi(String query) {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();
        try {
            System.out.println("Initiating USDA API search for: " + query);
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            // Using USDA FoodData Central API for generic food lists
            String url = "https://api.nal.usda.gov/fdc/v1/foods/search?query=" + encodedQuery + "&pageSize=5&api_key=cVuSa2ltJoN9hrks4cbdNDBrbe3IAztHtVCJFR4h";
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            System.out.println("DEBUG: Raw USDA API Response: " + response.getBody());
            if (!response.getStatusCode().is2xxSuccessful()) {
                 System.err.println("USDA API returned error code: " + response.getStatusCode());
                 return "[]";
            }

            JsonNode root = mapper.readTree(response.getBody());
            JsonNode foodsNode = root.path("foods");
            
            List<java.util.Map<String, Object>> mappedFoods = new ArrayList<>();
            for (JsonNode food : foodsNode) {
                String name = food.path("description").asText();
                String brand = food.path("brandOwner").asText("Generic");
                
                double cals = 150.0; // fallback
                JsonNode nutrients = food.path("foodNutrients");
                for (JsonNode nut : nutrients) {
                    if ("Energy".equalsIgnoreCase(nut.path("nutrientName").asText())) {
                        cals = nut.path("value").asDouble();
                        break;
                    }
                }
                
                mappedFoods.add(java.util.Map.of(
                    "name", name,
                    "brand", brand,
                    "calories", cals
                ));
            }
            System.out.println("Search successful. Found " + mappedFoods.size() + " matches.");
            return mapper.writeValueAsString(mappedFoods);
        } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {
            System.err.println("CRITICAL: USDA API Rate Limit Exceeded (429). Search disabled temporarily.");
            return "{\"error\": \"RATE_LIMIT_EXCEEDED\"}";
        } catch (Exception e) {
            System.err.println("Error during USDA API search: " + e.getMessage());
            e.printStackTrace();
            return "[]"; // Fallback empty array
        }
    }
}
