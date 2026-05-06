package com.neuroguard.assuranceservice.service;

import com.neuroguard.assuranceservice.config.SmsConfig;
import com.neuroguard.assuranceservice.entity.Notification;
import com.neuroguard.assuranceservice.repository.NotificationRepository;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class SmsNotificationService {

    @Autowired
    private SmsConfig smsConfig;

    @Autowired
    private NotificationRepository notificationRepository;

    public void sendSmsNotification(Long patientId, String phoneNumber, String type, String message) {
        log.info("💬 Attempting to send SMS to: {}", phoneNumber);
        log.info("   Type: {}", type);

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            log.warn("⚠️ Phone number is empty/null - SMS skipped");
            saveNotification(patientId, phoneNumber, type, "SMS", "SMS Alert", message, "FAILED",
                    "Phone number is empty or null");
            return;
        }

        // Check if Twilio is configured
        boolean twilioConfigured = smsConfig.isTwilioConfigured();
        log.info("   Twilio configured: {}", twilioConfigured);
        log.info("   Twilio SID: {}", smsConfig.getAccountSid() != null ? "SET" : "NULL");
        log.info("   Twilio Token: {}", smsConfig.getAuthToken() != null ? "SET" : "NULL");

        if (!twilioConfigured) {
            log.warn("⚠️ Twilio NOT configured - SMS skipped");
            saveNotification(patientId, phoneNumber, type, "SMS", "SMS Alert", message, "FAILED",
                    "Twilio not configured");
            return;
        }

        try {
            // Format phone number
            String formattedPhone = formatPhoneNumber(phoneNumber);
            log.info("   Original phone: {}", phoneNumber);
            log.info("   Formatted phone: {}", formattedPhone);
            log.info("   From phone (Twilio): {}", smsConfig.getFromPhoneNumber());

            // Send via Twilio
            log.info("   Sending via Twilio API...");
            Message smsMessage = Message.creator(
                    new PhoneNumber(formattedPhone),           // To number
                    new PhoneNumber(smsConfig.getFromPhoneNumber()),  // From number
                    message                                   // Body
            ).create();

            // Save to history
            saveNotification(patientId, phoneNumber, type, "SMS", "SMS Alert", message, "SENT", null);

            log.info("✅ SMS sent successfully!");
            log.info("   Recipient: {}", formattedPhone);
            log.info("   Message SID: {}", smsMessage.getSid());
            log.info("   Status: {}", smsMessage.getStatus());

        } catch (Exception e) {
            log.error("❌ Error sending SMS to {}: {} - {}", phoneNumber, e.getClass().getName(), e.getMessage(), e);
            saveNotification(patientId, phoneNumber, type, "SMS", "SMS Alert", message, "FAILED",
                    e.getMessage());
        }
    }

    private String formatPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            return "+1234567890"; // Fallback
        }

        // If starts with 0, convert to +33 (French format)
        if (phone.startsWith("0")) {
            return "+33" + phone.substring(1);
        }

        // If doesn't start with +, add it
        if (!phone.startsWith("+")) {
            return "+" + phone;
        }

        return phone;
    }

    private void saveNotification(Long patientId, String recipient, String type, String channel,
                                 String subject, String body, String status, String errorMessage) {
        try {
            Notification notification = Notification.builder()
                    .patientId(patientId)
                    .recipient(recipient)
                    .type(type)
                    .channel(channel)
                    .subject(subject)
                    .body(body)
                    .status(status)
                    .errorMessage(errorMessage)
                    .sentAt(status.equals("SENT") ? LocalDateTime.now() : null)
                    .build();

            notificationRepository.save(notification);
        } catch (Exception e) {
            log.error("Failed to save SMS notification history: {}", e.getMessage());
        }
    }
}
