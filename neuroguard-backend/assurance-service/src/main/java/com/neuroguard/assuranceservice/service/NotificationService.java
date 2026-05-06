package com.neuroguard.assuranceservice.service;

import com.neuroguard.assuranceservice.dto.NotificationRequest;
import com.neuroguard.assuranceservice.entity.Notification;

import java.util.List;
import java.util.Map;

public interface NotificationService {

    /**
     * Send notification through configured channels (email/SMS)
     */
    void sendNotification(NotificationRequest request);

    /**
     * Send email notification
     */
    void sendEmailNotification(Long patientId, String email, String type,
                              String subject, Map<String, Object> variables);

    /**
     * Send SMS notification
     */
    void sendSmsNotification(Long patientId, String phoneNumber, String type, String message);

    /**
     * Get notification history for a patient
     */
    List<Notification> getNotificationHistory(Long patientId);

    /**
     * Get notifications by type
     */
    List<Notification> getNotificationsByType(String type);

    /**
     * Get failed notifications
     */
    List<Notification> getFailedNotifications();
}
