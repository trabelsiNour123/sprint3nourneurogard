package com.neuroguard.assuranceservice.service;

import com.neuroguard.assuranceservice.dto.AssuranceRequestDto;
import com.neuroguard.assuranceservice.dto.AssuranceResponseDto;
import com.neuroguard.assuranceservice.dto.NotificationRequest;
import com.neuroguard.assuranceservice.dto.UserDto;
import com.neuroguard.assuranceservice.entity.Assurance;
import com.neuroguard.assuranceservice.entity.AssuranceStatus;
import com.neuroguard.assuranceservice.repository.AssuranceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssuranceServiceImpl implements AssuranceService {

    private final AssuranceRepository assuranceRepository;
    private final UserServiceClient userServiceClient;

    @Autowired(required = false)
    private NotificationService notificationService;

    @Override
    @Transactional
    public AssuranceResponseDto createAssurance(AssuranceRequestDto request) {
        log.info("Creating new assurance for patient: {}", request.getPatientId());

        // Verify patient exists
        UserDto patient = userServiceClient.getUserById(request.getPatientId());
        if (patient == null) {
            throw new RuntimeException("Patient not found with ID: " + request.getPatientId());
        }

        Assurance assurance = new Assurance();
        assurance.setPatientId(request.getPatientId());
        assurance.setProviderName(request.getProviderName());
        assurance.setPolicyNumber(request.getPolicyNumber());
        assurance.setCoverageDetails(request.getCoverageDetails());
        assurance.setIllness(request.getIllness());
        assurance.setPostalCode(request.getPostalCode());
        assurance.setMobilePhone(request.getMobilePhone());
        assurance.setStatus(AssuranceStatus.PENDING);

        Assurance saved = assuranceRepository.save(assurance);

        // Send notification for new assurance
        sendNotificationAsync("ASSURANCE_CREATED", saved, patient);

        return mapToDto(saved, patient);
    }

    @Override
    public List<AssuranceResponseDto> getAssurancesByPatient(Long patientId) {
        log.info("Fetching assurances for patient: {}", patientId);
        
        // Try to fetch user info, fallback to null if user-service unavailable
        UserDto patient = null;
        try {
            patient = userServiceClient.getUserById(patientId);
            log.info("Successfully fetched user info for patient: {}", patientId);
        } catch (Exception e) {
            log.warn("User service unavailable, returning assurances without user info: {}", e.getMessage());
        }
        
        final UserDto userInfo = patient;
        return assuranceRepository.findByPatientId(patientId).stream()
                .map(assurance -> mapToDto(assurance, userInfo))
                .collect(Collectors.toList());
    }

    @Override
    public List<AssuranceResponseDto> getAllAssurances() {
        log.info("Fetching all assurances");
        return assuranceRepository.findAll().stream()
                .map(assurance -> {
                    UserDto patient = null;
                    try {
                        patient = userServiceClient.getUserById(assurance.getPatientId());
                    } catch(Exception e) {
                        log.warn("Could not fetch user with ID: {}", assurance.getPatientId());
                    }
                    return mapToDto(assurance, patient);
                })
                .collect(Collectors.toList());
    }

    @Override
    public AssuranceResponseDto getAssuranceById(Long id) {
        Assurance assurance = assuranceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assurance not found with id: " + id));
        UserDto patient = userServiceClient.getUserById(assurance.getPatientId());
        return mapToDto(assurance, patient);
    }

    @Override
    public List<AssuranceResponseDto> getAssurancesByIds(List<Long> ids) {
        log.info("Fetching assurances by IDs: {} assurance(s)", ids.size());
        return assuranceRepository.findAllById(ids).stream()
                .map(assurance -> {
                    UserDto patient = null;
                    try {
                        patient = userServiceClient.getUserById(assurance.getPatientId());
                    } catch(Exception e) {
                        log.warn("Could not fetch user with ID: {}", assurance.getPatientId());
                    }
                    return mapToDto(assurance, patient);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AssuranceResponseDto updateAssuranceStatus(Long id, AssuranceStatus status) {
        log.info("Updating status for assurance: {} to {}", id, status);
        Assurance assurance = assuranceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assurance not found with id: " + id));

        assurance.setStatus(status);
        Assurance updated = assuranceRepository.save(assurance);

        UserDto patient = null;
        try {
            patient = userServiceClient.getUserById(updated.getPatientId());
        } catch(Exception e) {
            log.warn("Could not fetch user with ID: {}", updated.getPatientId());
        }

        // Send notification for status update
        sendNotificationAsync("ASSURANCE_" + status.toString(), updated, patient);

        return mapToDto(updated, patient);
    }

    @Override
    public AssuranceResponseDto updateAssurance(Long id, AssuranceRequestDto request) {
        log.info("Updating assurance: {}", id);
        Assurance assurance = assuranceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assurance not found with id: " + id));

        // Verify patient exists
        UserDto patient = userServiceClient.getUserById(request.getPatientId());
        if (patient == null) {
            throw new RuntimeException("Patient not found with ID: " + request.getPatientId());
        }

        assurance.setPatientId(request.getPatientId());
        assurance.setProviderName(request.getProviderName());
        assurance.setPolicyNumber(request.getPolicyNumber());
        assurance.setCoverageDetails(request.getCoverageDetails());
        assurance.setIllness(request.getIllness());
        assurance.setPostalCode(request.getPostalCode());
        assurance.setMobilePhone(request.getMobilePhone());

        Assurance updated = assuranceRepository.save(assurance);
        return mapToDto(updated, patient);
    }

    @Override
    public void deleteAssurance(Long id) {
        log.info("Deleting assurance: {}", id);
        Assurance assurance = assuranceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assurance not found with id: " + id));
        assuranceRepository.delete(assurance);
    }

    /**
     * Send notification asynchronously (non-blocking)
     */
    @Async
    public void sendNotificationAsync(String type, Assurance assurance, UserDto patient) {
        log.info("🔔 sendNotificationAsync called - Type: {}, PatientId: {}", type, assurance.getPatientId());

        if (notificationService == null) {
            log.error("❌ Notification service NOT AVAILABLE (null)");
            return;
        }

        log.info("✓ NotificationService is available");

        try {
            log.info("📧 Building notification request for type: {}", type);
            NotificationRequest notification = new NotificationRequest();
            notification.setPatientId(assurance.getPatientId());
            notification.setEmail(patient != null ? patient.getEmail() : null);

            // Phone number: try patient profile first, then assurance.mobilePhone
            String phoneNumber = null;
            if (patient != null && patient.getPhoneNumber() != null && !patient.getPhoneNumber().isEmpty()) {
                phoneNumber = patient.getPhoneNumber();
            } else if (assurance.getMobilePhone() != null && !assurance.getMobilePhone().isEmpty()) {
                phoneNumber = assurance.getMobilePhone();
            }
            notification.setPhoneNumber(phoneNumber);
            notification.setType(type);

            // Set subject based on type
            switch(type) {
                case "ASSURANCE_CREATED":
                    notification.setSubject("Your insurance application has been received");
                    break;
                case "ASSURANCE_APPROVED":
                    notification.setSubject("Your insurance has been APPROVED!");
                    break;
                case "ASSURANCE_REJECTED":
                    notification.setSubject("Your insurance application has been REJECTED");
                    break;
                default:
                    notification.setSubject("Insurance notification");
            }

            // Set template variables
            Map<String, Object> variables = new HashMap<>();
            variables.put("patientName", patient != null ? patient.getFirstName() + " " + patient.getLastName() : "Valued Patient");
            variables.put("providerName", assurance.getProviderName());
            variables.put("policyNumber", assurance.getPolicyNumber());
            variables.put("status", assurance.getStatus().toString());
            variables.put("illness", assurance.getIllness());
            notification.setTemplateVariables(variables);

            // Send through both email and SMS
            notification.setChannels(Arrays.asList("EMAIL", "SMS"));

            log.info("📤 Sending notification via channels: {}", notification.getChannels());
            log.info("   Email: {}", notification.getEmail());
            log.info("   Phone: {}", notification.getPhoneNumber());

            notificationService.sendNotification(notification);
            log.info("✅ Notification sent successfully");
        } catch (Exception e) {
            log.error("❌ Error sending notification: {}", e.getMessage(), e);
        }
    }

    private AssuranceResponseDto mapToDto(Assurance assurance, UserDto patient) {
        AssuranceResponseDto dto = new AssuranceResponseDto();
        dto.setId(assurance.getId());
        dto.setPatientId(assurance.getPatientId());
        dto.setPatientDetails(patient);
        dto.setProviderName(assurance.getProviderName());
        dto.setPolicyNumber(assurance.getPolicyNumber());
        dto.setCoverageDetails(assurance.getCoverageDetails());
        dto.setIllness(assurance.getIllness());
        dto.setPostalCode(assurance.getPostalCode());
        dto.setMobilePhone(assurance.getMobilePhone());
        dto.setStatus(assurance.getStatus());
        dto.setCreatedAt(assurance.getCreatedAt());
        dto.setUpdatedAt(assurance.getUpdatedAt());
        return dto;
    }
}

