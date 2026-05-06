package com.neuroguard.riskalertservice.service;

import com.neuroguard.riskalertservice.client.MedicalHistoryClient;
import com.neuroguard.riskalertservice.client.MedicalHistoryProviderClient;
import com.neuroguard.riskalertservice.client.MlPredictorClient;
import com.neuroguard.riskalertservice.client.UserServiceClient;
import com.neuroguard.riskalertservice.dto.*;
import com.neuroguard.riskalertservice.entity.Alert;
import com.neuroguard.riskalertservice.repository.AlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Alert Service Tests")
class AlertServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private MedicalHistoryClient medicalHistoryClient;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private MlPredictorClient mlPredictorClient;

    @Mock
    private MedicalHistoryProviderClient medicalHistoryProviderClient;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private AlertService alertService;

    private UserDto patient;
    private MedicalHistorySummary historySummary;
    private PatientFeatures patientFeatures;
    private PredictionResponse predictionResponse;

    @BeforeEach
    void setUp() {
        patient = new UserDto();
        patient.setId(1L);
        patient.setFirstName("John");
        patient.setLastName("Doe");
        patient.setRole("PATIENT");
        patient.setDateOfBirth(LocalDate.of(1945, 1, 1));

        historySummary = new MedicalHistorySummary();
        historySummary.setPatientId(1L);
        historySummary.setDiagnosis("Alzheimer's Disease");
        historySummary.setDiagnosisDate(LocalDate.now().minusYears(3));
        historySummary.setProgressionStage("SEVERE");
        historySummary.setGeneticRisk("High");
        historySummary.setFamilyHistory("Family history present");
        historySummary.setEnvironmentalFactors("Low education level");
        historySummary.setComorbidities("Hypertension");
        historySummary.setCaregiverIds(Arrays.asList(2L, 4L, 5L));
        historySummary.setMmse(16);
        historySummary.setFunctionalAssessment(3);
        historySummary.setAdl(2);
        historySummary.setMemoryComplaints(true);
        historySummary.setBehavioralProblems(true);

        patientFeatures = new PatientFeatures();
        patientFeatures.setPatientId(1L);
        patientFeatures.setAge(79);
        patientFeatures.setGender("Male");
        patientFeatures.setProgressionStage("SEVERE");
        patientFeatures.setYearsSinceDiagnosis(3);
        patientFeatures.setComorbidityCount(2);
        patientFeatures.setAllergyCount(1);
        patientFeatures.setHasGeneticRisk(true);
        patientFeatures.setHasFamilyHistory(true);
        patientFeatures.setCaregiverCount(2);
        patientFeatures.setProviderCount(1);

        predictionResponse = new PredictionResponse();
        predictionResponse.setPatientId(1L);
        predictionResponse.setPrediction(1);
        predictionResponse.setProbability(0.91);
        predictionResponse.setRiskLevel("HIGH");
        predictionResponse.setRiskPercentage(91.0);
        predictionResponse.setRecommendation("Immediate follow-up required");
    }

    @Test
    @DisplayName("Should generate automatic alerts for all patients")
    void testGenerateAlertsForAllPatients_Success() {
        MedicalHistorySummary minimalHistory = new MedicalHistorySummary();
        minimalHistory.setPatientId(1L);
        minimalHistory.setProgressionStage("SEVERE");

        when(userServiceClient.getUsersByRole("PATIENT")).thenReturn(List.of(patient));
        when(medicalHistoryProviderClient.getMedicalHistoryByPatientId(1L)).thenReturn(minimalHistory);
        when(alertRepository.existsByPatientIdAndMessageAndResolvedFalse(eq(1L), anyString())).thenReturn(false);
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> {
            Alert saved = invocation.getArgument(0);
            saved.setId(10L);
            saved.setCreatedAt(LocalDateTime.now());
            saved.setUpdatedAt(LocalDateTime.now());
            return saved;
        });
        when(userServiceClient.getUserById(1L)).thenReturn(patient);

        alertService.generateAlertsForAllPatients();

        verify(alertRepository, atLeastOnce()).save(any(Alert.class));
        verify(messagingTemplate, times(2)).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    @DisplayName("Should do nothing when there are no patients")
    void testGenerateAlertsForAllPatients_NoPatients() {
        when(userServiceClient.getUsersByRole("PATIENT")).thenReturn(Collections.emptyList());

        alertService.generateAlertsForAllPatients();

        verifyNoInteractions(medicalHistoryProviderClient);
        verifyNoInteractions(alertRepository);
        verifyNoInteractions(messagingTemplate);
    }

    @Test
    @DisplayName("Should create manual alert successfully")
    void testCreateAlert_Success() {
        AlertRequest request = new AlertRequest();
        request.setPatientId(1L);
        request.setMessage("Manual alert message");
        request.setSeverity("WARNING");

        when(userServiceClient.getUserById(1L)).thenReturn(patient);
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> {
            Alert saved = invocation.getArgument(0);
            saved.setId(22L);
            saved.setCreatedAt(LocalDateTime.now());
            saved.setUpdatedAt(LocalDateTime.now());
            return saved;
        });

        AlertResponse response = alertService.createAlert(request, 3L);

        assertNotNull(response);
        assertEquals(1L, response.getPatientId());
        assertEquals("Manual alert message", response.getMessage());
        assertEquals("WARNING", response.getSeverity());
        verify(alertRepository, times(1)).save(any(Alert.class));
        verify(messagingTemplate, times(2)).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    @DisplayName("Should throw when patient does not exist during alert creation")
    void testCreateAlert_PatientNotFoundThrows() {
        AlertRequest request = new AlertRequest();
        request.setPatientId(404L);
        request.setMessage("Manual alert message");
        request.setSeverity("WARNING");

        when(userServiceClient.getUserById(404L)).thenReturn(null);
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AlertResponse response = alertService.createAlert(request, 3L);

        assertNotNull(response);
        assertEquals(404L, response.getPatientId());
        verify(alertRepository, times(1)).save(any(Alert.class));
    }

    @Test
    @DisplayName("Should continue creating alert when user-service check fails")
    void testCreateAlert_UserServiceFailureStillCreates() {
        AlertRequest request = new AlertRequest();
        request.setPatientId(1L);
        request.setMessage("Manual alert message");
        request.setSeverity("INFO");

        when(userServiceClient.getUserById(1L)).thenThrow(new RuntimeException("service unavailable"));
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AlertResponse response = alertService.createAlert(request, 3L);

        assertNotNull(response);
        verify(alertRepository, times(1)).save(any(Alert.class));
    }

    @Test
    @DisplayName("Should update alert successfully")
    void testUpdateAlert_Success() {
        Alert alert = new Alert();
        alert.setId(11L);
        alert.setPatientId(1L);
        alert.setMessage("Old message");
        alert.setSeverity("INFO");
        alert.setResolved(false);
        alert.setCreatedAt(LocalDateTime.now());
        alert.setUpdatedAt(LocalDateTime.now());

        AlertRequest request = new AlertRequest();
        request.setPatientId(1L);
        request.setMessage("Updated message");
        request.setSeverity("CRITICAL");

        when(alertRepository.findById(11L)).thenReturn(Optional.of(alert));
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userServiceClient.getUserById(1L)).thenReturn(patient);

        AlertResponse response = alertService.updateAlert(11L, request, 3L);

        assertNotNull(response);
        assertEquals("Updated message", response.getMessage());
        assertEquals("CRITICAL", response.getSeverity());
        verify(alertRepository, times(1)).save(any(Alert.class));
    }

    @Test
    @DisplayName("Should throw when updating missing alert")
    void testUpdateAlert_NotFoundThrows() {
        AlertRequest request = new AlertRequest();
        request.setPatientId(1L);
        request.setMessage("Updated message");
        request.setSeverity("CRITICAL");

        when(alertRepository.findById(11L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> alertService.updateAlert(11L, request, 3L));

        assertTrue(exception.getMessage().contains("Alert not found"));
        verify(alertRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete alert successfully")
    void testDeleteAlert_Success() {
        Alert alert = new Alert();
        alert.setId(11L);
        alert.setPatientId(1L);
        alert.setMessage("Delete me");
        alert.setSeverity("INFO");
        alert.setResolved(false);

        when(alertRepository.findById(11L)).thenReturn(Optional.of(alert));
        when(userServiceClient.getUserById(1L)).thenReturn(patient);

        alertService.deleteAlert(11L, 3L);

        verify(alertRepository, times(1)).delete(alert);
        verify(messagingTemplate, times(2)).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    @DisplayName("Should resolve alert for provider successfully")
    void testResolveAlert_Success() {
        Alert alert = new Alert();
        alert.setId(11L);
        alert.setPatientId(1L);
        alert.setMessage("Resolve me");
        alert.setSeverity("WARNING");
        alert.setResolved(false);
        alert.setCreatedAt(LocalDateTime.now());
        alert.setUpdatedAt(LocalDateTime.now());

        when(alertRepository.findById(11L)).thenReturn(Optional.of(alert));
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userServiceClient.getUserById(1L)).thenReturn(patient);

        AlertResponse response = alertService.resolveAlert(11L, 3L);

        assertNotNull(response);
        assertTrue(response.isResolved());
        verify(alertRepository, times(1)).save(any(Alert.class));
    }

    @Test
    @DisplayName("Should get alerts for a patient")
    void testGetAlertsForPatient_Success() {
        Alert alert = new Alert();
        alert.setId(1L);
        alert.setPatientId(1L);
        alert.setMessage("Patient alert");
        alert.setSeverity("INFO");
        alert.setResolved(false);
        alert.setCreatedAt(LocalDateTime.now());
        alert.setUpdatedAt(LocalDateTime.now());

        when(userServiceClient.getUserById(1L)).thenReturn(patient);
        when(alertRepository.findByPatientId(1L)).thenReturn(List.of(alert));

        List<AlertResponse> responses = alertService.getAlertsForPatient(1L, 1L, "PATIENT");

        assertEquals(1, responses.size());
        assertEquals("Patient alert", responses.get(0).getMessage());
    }

    @Test
    @DisplayName("Should deny access when patient requests another patient's alerts")
    void testGetAlertsForPatient_AccessDenied() {
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> alertService.getAlertsForPatient(1L, 2L, "PATIENT"));

        assertTrue(exception.getMessage().contains("Access denied"));
    }

    @Test
    @DisplayName("Should get caregiver alerts for assigned patients")
    void testGetAlertsForCaregiverPatients_Success() {
        UserDto patient1 = new UserDto();
        patient1.setId(1L);
        patient1.setFirstName("John");
        patient1.setLastName("Doe");

        UserDto patient2 = new UserDto();
        patient2.setId(2L);
        patient2.setFirstName("Mary");
        patient2.setLastName("Jones");

        Alert alert1 = new Alert();
        alert1.setId(1L);
        alert1.setPatientId(1L);
        alert1.setMessage("Alert 1");
        alert1.setSeverity("INFO");
        alert1.setResolved(false);
        alert1.setCreatedAt(LocalDateTime.now());
        alert1.setUpdatedAt(LocalDateTime.now());

        Alert alert2 = new Alert();
        alert2.setId(2L);
        alert2.setPatientId(2L);
        alert2.setMessage("Alert 2");
        alert2.setSeverity("WARNING");
        alert2.setResolved(false);
        alert2.setCreatedAt(LocalDateTime.now());
        alert2.setUpdatedAt(LocalDateTime.now());

        when(medicalHistoryClient.getAssignedPatientsForCaregiver())
                .thenReturn(Arrays.asList(patient1, patient2));
        when(alertRepository.findByPatientIdIn(Arrays.asList(1L, 2L))).thenReturn(Arrays.asList(alert1, alert2));
        when(userServiceClient.getUserById(1L)).thenReturn(patient1);
        when(userServiceClient.getUserById(2L)).thenReturn(patient2);

        List<AlertResponse> responses = alertService.getAlertsForCaregiverPatients(5L);

        assertEquals(2, responses.size());
        verify(alertRepository, times(1)).findByPatientIdIn(Arrays.asList(1L, 2L));
    }

    @Test
    @DisplayName("Should return empty caregiver alert list when no patients are assigned")
    void testGetAlertsForCaregiverPatients_NoPatients() {
        when(medicalHistoryClient.getAssignedPatientsForCaregiver()).thenReturn(Collections.emptyList());

        List<AlertResponse> responses = alertService.getAlertsForCaregiverPatients(5L);

        assertTrue(responses.isEmpty());
        verifyNoInteractions(alertRepository);
    }

    @Test
    @DisplayName("Should resolve alert for patient successfully")
    void testResolveAlertForPatient_Success() {
        Alert alert = new Alert();
        alert.setId(9L);
        alert.setPatientId(1L);
        alert.setMessage("Patient resolve");
        alert.setSeverity("WARNING");
        alert.setResolved(false);
        alert.setCreatedAt(LocalDateTime.now());
        alert.setUpdatedAt(LocalDateTime.now());

        when(alertRepository.findById(9L)).thenReturn(Optional.of(alert));
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userServiceClient.getUserById(1L)).thenReturn(patient);

        AlertResponse response = alertService.resolveAlertForPatient(9L, 1L);

        assertNotNull(response);
        assertTrue(response.isResolved());
        verify(alertRepository, times(1)).save(any(Alert.class));
    }

    @Test
    @DisplayName("Should resolve alert for caregiver successfully")
    void testResolveAlertForCaregiver_Success() {
        Alert alert = new Alert();
        alert.setId(10L);
        alert.setPatientId(1L);
        alert.setMessage("Caregiver resolve");
        alert.setSeverity("CRITICAL");
        alert.setResolved(false);
        alert.setCreatedAt(LocalDateTime.now());
        alert.setUpdatedAt(LocalDateTime.now());

        when(alertRepository.findById(10L)).thenReturn(Optional.of(alert));
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userServiceClient.getUserById(1L)).thenReturn(patient);

        AlertResponse response = alertService.resolveAlertForCaregiver(10L, 2L);

        assertNotNull(response);
        assertTrue(response.isResolved());
        verify(alertRepository, times(1)).save(any(Alert.class));
    }

    @Test
    @DisplayName("Should generate predictive alert for patient")
    void testGeneratePredictiveAlertForPatient_Success() {
        when(medicalHistoryProviderClient.getPatientFeatures(1L)).thenReturn(patientFeatures);
        when(mlPredictorClient.predict(any(PredictionRequest.class))).thenReturn(predictionResponse);
        when(alertRepository.existsByPatientIdAndRiskLevelAndResolvedFalse(1L, "HIGH")).thenReturn(false);
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> {
            Alert saved = invocation.getArgument(0);
            saved.setId(77L);
            saved.setCreatedAt(LocalDateTime.now());
            saved.setUpdatedAt(LocalDateTime.now());
            return saved;
        });
        when(userServiceClient.getUserById(1L)).thenReturn(patient);

        alertService.generatePredictiveAlertForPatient(1L);

        verify(alertRepository, times(1)).save(any(Alert.class));
        verify(messagingTemplate, times(2)).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    @DisplayName("Should skip duplicate predictive alert")
    void testGeneratePredictiveAlertForPatient_DuplicateSkipped() {
        when(medicalHistoryProviderClient.getPatientFeatures(1L)).thenReturn(patientFeatures);
        when(mlPredictorClient.predict(any(PredictionRequest.class))).thenReturn(predictionResponse);
        when(alertRepository.existsByPatientIdAndRiskLevelAndResolvedFalse(1L, "HIGH")).thenReturn(true);

        alertService.generatePredictiveAlertForPatient(1L);

        verify(alertRepository, never()).save(any());
        verifyNoInteractions(messagingTemplate);
    }

    @Test
    @DisplayName("Should generate predictive alerts for all patients")
    void testGeneratePredictiveAlertsForAllPatients_Success() {
        when(userServiceClient.getUsersByRole("PATIENT")).thenReturn(List.of(patient));
        when(medicalHistoryProviderClient.getPatientFeatures(1L)).thenReturn(patientFeatures);
        when(mlPredictorClient.predict(any(PredictionRequest.class))).thenReturn(predictionResponse);
        when(alertRepository.existsByPatientIdAndRiskLevelAndResolvedFalse(1L, "HIGH")).thenReturn(false);
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userServiceClient.getUserById(1L)).thenReturn(patient);

        alertService.generatePredictiveAlertsForAllPatients();

        verify(alertRepository, times(1)).save(any(Alert.class));
    }
}
