package com.neuroguard.assuranceservice.service;

import com.neuroguard.assuranceservice.dto.NotificationRequest;
import com.neuroguard.assuranceservice.entity.Notification;
import com.neuroguard.assuranceservice.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private EmailNotificationService emailNotificationService;

    @Autowired
    private SmsNotificationService smsNotificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public void sendNotification(NotificationRequest request) {
        if (request == null) {
            log.warn("Null notification request received");
            return;
        }

        // Default to EMAIL if no channels specified
        if (request.getChannels() == null || request.getChannels().isEmpty()) {
            request.setChannels(Arrays.asList("EMAIL"));
        }

        // Send through each channel
        for (String channel : request.getChannels()) {
            if ("EMAIL".equalsIgnoreCase(channel) && request.getEmail() != null) {
                sendEmailNotification(request.getPatientId(), request.getEmail(),
                        request.getType(), request.getSubject(), request.getTemplateVariables());

            } else if ("SMS".equalsIgnoreCase(channel) && request.getPhoneNumber() != null) {
                String message = buildSmsMessage(request.getType(), request.getTemplateVariables());
                sendSmsNotification(request.getPatientId(), request.getPhoneNumber(),
                        request.getType(), message);
            }
        }
    }

    @Override
    public void sendEmailNotification(Long patientId, String email, String type,
                                     String subject, Map<String, Object> variables) {
        if (emailNotificationService != null) {
            emailNotificationService.sendEmailNotification(patientId, email, type, subject, variables);
        } else {
            log.warn("Email notification service not available");
        }
    }

    @Override
    public void sendSmsNotification(Long patientId, String phoneNumber, String type, String message) {
        if (smsNotificationService != null) {
            smsNotificationService.sendSmsNotification(patientId, phoneNumber, type, message);
        } else {
            log.warn("SMS notification service not available");
        }
    }

    @Override
    public List<Notification> getNotificationHistory(Long patientId) {
        return notificationRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    @Override
    public List<Notification> getNotificationsByType(String type) {
        return notificationRepository.findByType(type);
    }

    @Override
    public List<Notification> getFailedNotifications() {
        return notificationRepository.findByStatus("FAILED");
    }

    /**
     * Build a concise SMS message from notification type and variables
     */
    private String buildSmsMessage(String type, Map<String, Object> variables) {
        if (variables == null) {
            return "Notification from NeuroGuard";
        }

        switch (type) {
            case "ASSURANCE_APPROVED":
                Object provider = variables.getOrDefault("providerName", "Insurance Provider");
                return String.format("✓ Your insurance has been APPROVED! Provider: %s - NeuroGuard", provider);

            case "ASSURANCE_REJECTED":
                return "✗ Your insurance application has been REJECTED. Please contact us for details - NeuroGuard";

            case "ASSURANCE_CREATED":
                return "Your insurance application has been received and is pending review - NeuroGuard";

            default:
                return "Important notification from NeuroGuard";
        }
    }
}
