package com.neuroguard.monitoringservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuroguard.monitoringservice.dto.MealDTO;
import com.neuroguard.monitoringservice.dto.NutritionDTO;
import com.neuroguard.monitoringservice.service.NutritionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NutritionController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for integration testing of controller logic
public class NutritionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NutritionService nutritionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetNutritionToday_ShouldReturnNutritionDTO() throws Exception {
        // Arrange
        String patientId = "patient-123";
        NutritionDTO dto = new NutritionDTO();
        dto.setPatientId(patientId);
        dto.setDate(LocalDate.now());
        dto.setDailyCalories(1200);
        dto.setMeals(new ArrayList<>());

        when(nutritionService.getDailyNutrition(eq(patientId), any(LocalDate.class))).thenReturn(dto);

        // Act & Assert
        mockMvc.perform(get("/nutrition/" + patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(patientId))
                .andExpect(jsonPath("$.dailyCalories").value(1200));
    }

    @Test
    void testAddMeal_ShouldReturnUpdatedNutritionDTO() throws Exception {
        // Arrange
        String patientId = "patient-123";
        MealDTO mealDTO = new MealDTO(null, "Salad", "LUNCH", true, 300);
        
        NutritionDTO responseDTO = new NutritionDTO();
        responseDTO.setPatientId(patientId);
        responseDTO.setDailyCalories(300);
        responseDTO.setMeals(new ArrayList<>());

        when(nutritionService.addMeal(eq(patientId), any(MealDTO.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/nutrition/" + patientId + "/meals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mealDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyCalories").value(300));
    }
}
