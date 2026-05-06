package com.neuroguard.riskalertservice.service;

import com.neuroguard.riskalertservice.client.MedicalHistoryClient;
import com.neuroguard.riskalertservice.client.MedicalHistoryProviderClient;
import com.neuroguard.riskalertservice.client.MlPredictorClient;
import com.neuroguard.riskalertservice.client.UserServiceClient;
import com.neuroguard.riskalertservice.dto.*;
import com.neuroguard.riskalertservice.entity.Alert;
import com.neuroguard.riskalertservice.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private final AlertRepository alertRepository;
    private final MedicalHistoryClient medicalHistoryClient;
    private final UserServiceClient userServiceClient;
    private final MlPredictorClient mlPredictorClient;
    private final MedicalHistoryProviderClient medicalHistoryProviderClient;
    private final SimpMessagingTemplate messagingTemplate;

    // ------------------- Automatic Generation (scheduled & on-demand) -------------------
    @Transactional
    public void generateAlertsForAllPatients() {
        try {
            List<UserDto> patients = userServiceClient.getUsersByRole("PATIENT");
            if (patients == null || patients.isEmpty()) {
                log.warn("No patients found or user-service returned null");
                return;
            }
            for (UserDto patient : patients) {
                try {
                    MedicalHistorySummary history = medicalHistoryProviderClient.getMedicalHistoryByPatientId(patient.getId());
                    generateAlertsForPatient(patient, history);
                } catch (feign.FeignException.Unauthorized e) {
                    log.error("Unauthorized access to medical-history for patient {}: {}", patient.getId(), e.getMessage());
                } catch (feign.FeignException.NotFound e) {
                    log.debug("Medical history not found for patient {}", patient.getId());
                } catch (Exception e) {
                    log.error("Failed to generate alerts for patient {}: {} ({})", patient.getId(), e.getMessage(), e.getClass().getSimpleName());
                }
            }
        } catch (feign.FeignException.ServiceUnavailable e) {
            log.error("User-service is unavailable: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Failed to fetch patients for alert generation: {} ({})", e.getMessage(), e.getClass().getSimpleName());
        }
    }

    private void generateAlertsForPatient(UserDto patient, MedicalHistorySummary history) {
        // Rule 1: Severe progression stage → CRITICAL
        if ("SEVERE".equalsIgnoreCase(history.getProgressionStage())) {
            createAutoAlert(patient.getId(), "Progression stage is SEVERE. Immediate attention required.", "CRITICAL");
        }

        // Rule 2: Moderate progression + age > 75 → WARNING (increased fall risk)
        if ("MODERATE".equalsIgnoreCase(history.getProgressionStage()) && patient.getDateOfBirth() != null) {
            int age = Period.between(patient.getDateOfBirth(), LocalDate.now()).getYears();
            if (age > 75) {
                createAutoAlert(patient.getId(), "Moderate progression and age > 75 – high fall risk.", "WARNING");
            }
        }

        // Rule 3: Any allergy recorded → WARNING
        if (hasAllergies(history)) {
            createAutoAlert(patient.getId(), "Patient has allergies that may require attention.", "WARNING");
        }

        // Rule 4: Comorbidities present → INFO (with details)
        if (history.getComorbidities() != null && !history.getComorbidities().isBlank()) {
            createAutoAlert(patient.getId(), "Comorbidities detected: " + history.getComorbidities(), "INFO");
        }

        // Rule 5: Genetic risk factors → INFO
        if (history.getGeneticRisk() != null && !history.getGeneticRisk().isBlank()) {
            createAutoAlert(patient.getId(), "Genetic risk factors recorded: " + history.getGeneticRisk(), "INFO");
        }

        // Rule 6: Family history of note → INFO
        if (history.getFamilyHistory() != null && !history.getFamilyHistory().isBlank()) {
            createAutoAlert(patient.getId(), "Family history recorded: " + history.getFamilyHistory(), "INFO");
        }

        // Rule 7: Environmental factors → INFO
        if (history.getEnvironmentalFactors() != null && !history.getEnvironmentalFactors().isBlank()) {
            createAutoAlert(patient.getId(), "Environmental factors: " + history.getEnvironmentalFactors(), "INFO");
        }

        // Rule 8: Diagnosis recorded → INFO
        if (history.getDiagnosis() != null && !history.getDiagnosis().isBlank()) {
            createAutoAlert(patient.getId(), "Diagnosis: " + history.getDiagnosis(), "INFO");
        }

        // Rule 9: Diagnosis older than 2 years → suggest re-evaluation
        if (history.getDiagnosisDate() != null) {
            int yearsSinceDiagnosis = Period.between(history.getDiagnosisDate(), LocalDate.now()).getYears();
            if (yearsSinceDiagnosis >= 2) {
                createAutoAlert(patient.getId(), "Diagnosis was " + yearsSinceDiagnosis + " years ago. Consider re-evaluation.", "INFO");
            }
        }

        // Rule 10: Multiple caregivers assigned → coordination needed (optional)
        if (history.getCaregiverIds() != null && history.getCaregiverIds().size() > 2) {
            createAutoAlert(patient.getId(), "Multiple caregivers assigned – ensure consistent communication.", "INFO");
        }

        // NEW RULES using added cognitive scores
        // Rule 11: Low MMSE score (< 18) → WARNING
        if (history.getMmse() != null && history.getMmse() < 18) {
            createAutoAlert(patient.getId(), "Low MMSE score (" + history.getMmse() + ") indicates significant cognitive impairment.", "WARNING");
        }

        // Rule 12: Low FunctionalAssessment (< 4) → WARNING
        if (history.getFunctionalAssessment() != null && history.getFunctionalAssessment() < 4) {
            createAutoAlert(patient.getId(), "Low functional assessment score (" + history.getFunctionalAssessment() + ") suggests declining independence.", "WARNING");
        }

        // Rule 13: Low ADL (< 4) → WARNING
        if (history.getAdl() != null && history.getAdl() < 4) {
            createAutoAlert(patient.getId(), "Low ADL score (" + history.getAdl() + ") indicates difficulties with daily activities.", "WARNING");
        }

        // Rule 14: Memory complaints reported → INFO
        if (Boolean.TRUE.equals(history.getMemoryComplaints())) {
            createAutoAlert(patient.getId(), "Patient reports memory complaints.", "INFO");
        }

        // Rule 15: Behavioral problems reported → WARNING
        if (Boolean.TRUE.equals(history.getBehavioralProblems())) {
            createAutoAlert(patient.getId(), "Behavioral problems reported – may require additional support.", "WARNING");
        }
    }

    private boolean hasAllergies(MedicalHistorySummary history) {
        return (history.getMedicationAllergies() != null && !history.getMedicationAllergies().isBlank()) ||
                (history.getEnvironmentalAllergies() != null && !history.getEnvironmentalAllergies().isBlank()) ||
                (history.getFoodAllergies() != null && !history.getFoodAllergies().isBlank());
    }

    private void createAutoAlert(Long patientId, String message, String severity) {
        // Avoid duplicate unresolved alerts for the same patient and message
        if (!alertRepository.existsByPatientIdAndMessageAndResolvedFalse(patientId, message)) {
            Alert alert = new Alert();
            alert.setPatientId(patientId);
            alert.setMessage(message);
            alert.setSeverity(severity);
            alert.setResolved(false);
            alert.setCreatedBy(null); // auto-generated
            // riskLevel not set for rule-based alerts
            alertRepository.save(alert);
            log.info("Auto-generated alert for patient {}: {}", patientId, message);
            broadcastAlert("CREATE", mapToResponse(alert));
        }
    }

    // ------------------- Patient View -------------------
    public List<AlertResponse> getAlertsForPatient(Long patientId, Long requesterId, String requesterRole) {
        if (!requesterRole.equals("PATIENT") || !patientId.equals(requesterId)) {
            throw new RuntimeException("Access denied: You can only view your own alerts");
        }
        return alertRepository.findByPatientId(patientId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ------------------- Caregiver View -------------------
    public List<AlertResponse> getAlertsForCaregiverPatients(Long caregiverId) {
        // Fetch patients assigned to this caregiver from medical-history-service
        List<UserDto> assignedPatients = medicalHistoryClient.getAssignedPatientsForCaregiver(); // <- no argument
        List<Long> patientIds = assignedPatients.stream().map(UserDto::getId).collect(Collectors.toList());
        if (patientIds.isEmpty()) {
            return List.of();
        }
        return alertRepository.findByPatientIdIn(patientIds).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ------------------- Provider Operations -------------------
    @Transactional
    public AlertResponse createAlert(AlertRequest request, Long providerId) {
        // Verify patient exists before creating a manual alert
        try {
            UserDto patient = userServiceClient.getUserById(request.getPatientId());
            if (patient == null) {
                throw new RuntimeException("Patient not found");
            }
        } catch (feign.FeignException.NotFound e) {
            throw new RuntimeException("Patient not found");
        } catch (feign.FeignException.ServiceUnavailable e) {
            log.warn("User-service unavailable, but proceeding with alert creation for patient {}", request.getPatientId());
            // Continue - we'll still create the alert
        } catch (Exception e) {
            log.warn("Could not verify patient existence: {} - {}", e.getMessage(), e.getClass().getSimpleName());
            // Continue - we'll still create the alert
        }

        Alert alert = new Alert();
        alert.setPatientId(request.getPatientId());
        alert.setMessage(request.getMessage());
        alert.setSeverity(request.getSeverity());
        alert.setResolved(false);
        alert.setCreatedBy(providerId);
        alert = alertRepository.save(alert);
        AlertResponse response = mapToResponse(alert);
        broadcastAlert("CREATE", response);
        return response;
    }

    @Transactional
    public AlertResponse updateAlert(Long alertId, AlertRequest request, Long providerId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        log.info("Provider {} updating alert {}", providerId, alertId);
        alert.setMessage(request.getMessage());
        alert.setSeverity(request.getSeverity());
        alert = alertRepository.save(alert);
        AlertResponse response = mapToResponse(alert);
        broadcastAlert("UPDATE", response);
        return response;
    }

    @Transactional
    public void deleteAlert(Long alertId, Long providerId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        log.info("Provider {} deleting alert {}", providerId, alertId);
        AlertResponse response = mapToResponse(alert);
        alertRepository.delete(alert);
        broadcastAlert("DELETE", response);
    }

    @Transactional
    public AlertResponse resolveAlert(Long alertId, Long providerId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        log.info("Provider {} resolving alert {}", providerId, alertId);
        alert.setResolved(true);
        alert = alertRepository.save(alert);
        AlertResponse response = mapToResponse(alert);
        broadcastAlert("RESOLVE", response);
        return response;
    }

    @Transactional
    public AlertResponse resolveAlertForPatient(Long alertId, Long patientId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));
        log.info("Patient {} resolving alert {}", patientId, alertId);
        alert.setResolved(true);
        alert = alertRepository.save(alert);
        AlertResponse response = mapToResponse(alert);
        broadcastAlert("RESOLVE", response);
        return response;
    }

    @Transactional
    public AlertResponse resolveAlertForCaregiver(Long alertId, Long caregiverId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));
        log.info("Caregiver {} resolving alert {}", caregiverId, alertId);
        alert.setResolved(true);
        alert = alertRepository.save(alert);
        AlertResponse response = mapToResponse(alert);
        broadcastAlert("RESOLVE", response);
        return response;
    }

    public List<AlertResponse> getAlertsByPatientId(Long patientId) {
        return alertRepository.findByPatientId(patientId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ------------------- Helper -------------------
    private AlertResponse mapToResponse(Alert alert) {
        AlertResponse resp = new AlertResponse();
        resp.setId(alert.getId());
        resp.setPatientId(alert.getPatientId());
        try {
            UserDto patient = userServiceClient.getUserById(alert.getPatientId());
            if (patient != null) {
                resp.setPatientName(patient.getFirstName() + " " + patient.getLastName());
            } else {
                resp.setPatientName("Unknown");
                log.warn("User service returned null for patient {}", alert.getPatientId());
            }
        } catch (feign.FeignException.ServiceUnavailable e) {
            resp.setPatientName("Unknown");
            log.warn("User-service is unavailable for alert {}", alert.getId());
        } catch (feign.FeignException.Unauthorized e) {
            resp.setPatientName("Unknown");
            log.warn("Unauthorized access to user-service for alert {}", alert.getId());
        } catch (Exception e) {
            resp.setPatientName("Unknown");
            log.warn("Failed to fetch patient name for alert {}: {} ({})", alert.getId(), e.getMessage(), e.getClass().getSimpleName());
        }
        resp.setMessage(alert.getMessage());
        resp.setSeverity(alert.getSeverity());
        resp.setResolved(alert.isResolved());
        resp.setCreatedAt(alert.getCreatedAt());
        resp.setUpdatedAt(alert.getUpdatedAt());
        return resp;
    }

    // ------------------- Predictive alerts -------------------
    @Transactional
    public void generatePredictiveAlertForPatient(Long patientId) {
        try {
            // 1. Fetch patient features using the PROVIDER client
            PatientFeatures features = medicalHistoryProviderClient.getPatientFeatures(patientId);

            if (features == null) {
                log.warn("No patient features found for patient {}", patientId);
                return;
            }

            // 2. Prepare prediction request
            PredictionRequest request = new PredictionRequest();
            BeanUtils.copyProperties(features, request);

            // 3. Call ML service
            PredictionResponse response = mlPredictorClient.predict(request);

            if (response == null) {
                log.warn("No prediction response for patient {}", patientId);
                return;
            }

            // 4. Generate alerts based on ML risk assessment, but avoid duplicates by risk level
            generateAlertsFromPrediction(patientId, response);

        } catch (Exception e) {
            log.error("Failed to generate predictive alert for patient {}: {}", patientId, e.getMessage(), e);
        }
    }

    private void generateAlertsFromPrediction(Long patientId, PredictionResponse response) {
        String severity = mapRiskLevelToSeverity(response.getRiskLevel());
        String message = String.format(
                "ML Hospitalization Risk Assessment: %s (%.1f%%). %s",
                response.getRiskLevel(),
                response.getRiskPercentage(),
                response.getRecommendation()
        );

        // Use new duplicate detection: check by patient and risk level (not the whole message)
        if (!alertRepository.existsByPatientIdAndRiskLevelAndResolvedFalse(patientId, response.getRiskLevel())) {
            Alert alert = new Alert();
            alert.setPatientId(patientId);
            alert.setMessage(message);
            alert.setSeverity(severity);
            alert.setRiskLevel(response.getRiskLevel()); // store the risk level
            alert.setResolved(false);
            alert.setCreatedBy(null);
            alertRepository.save(alert);
            log.info("Predictive alert for patient {}: {} - Probability: {}",
                    patientId, response.getRiskLevel(), response.getRiskPercentage());
            broadcastAlert("CREATE", mapToResponse(alert));
        } else {
            log.debug("Skipped duplicate predictive alert for patient {} (risk level: {})",
                    patientId, response.getRiskLevel());
        }
    }

    private String mapRiskLevelToSeverity(String riskLevel) {
        return switch (riskLevel) {
            case "CRITICAL" -> "CRITICAL";
            case "HIGH" -> "WARNING";
            case "MODERATE" -> "INFO";
            default -> "INFO";
        };
    }

    public void generatePredictiveAlertsForAllPatients() {
        try {
            List<UserDto> patients = userServiceClient.getUsersByRole("PATIENT");
            if (patients == null || patients.isEmpty()) {
                log.warn("No patients found or user-service returned null");
                return;
            }
            log.info("Starting predictive alert generation for {} patients", patients.size());
            for (UserDto patient : patients) {
                generatePredictiveAlertForPatient(patient.getId());
            }
            log.info("Finished predictive alert generation for {} patients", patients.size());
        } catch (feign.FeignException.ServiceUnavailable e) {
            log.error("User-service is unavailable for predictive alert generation: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Failed to generate predictive alerts for all patients: {} ({})", e.getMessage(), e.getClass().getSimpleName());
        }
    }

    private void broadcastAlert(String action, AlertResponse alertResponse) {
        if (alertResponse != null) {
            WebSocketEvent<AlertResponse> event = new WebSocketEvent<>(action, "ALERT", alertResponse);
            // Patient/Caregiver get all events (CREATE, UPDATE, DELETE, RESOLVE)
            messagingTemplate.convertAndSend("/topic/alerts/patient/" + alertResponse.getPatientId(), event);
            // Provider only gets RESOLVE events via the provider topic
            // For CREATE/UPDATE/DELETE the provider updates its own UI optimistically
            messagingTemplate.convertAndSend("/topic/alerts/provider", event);
        }
    }
}