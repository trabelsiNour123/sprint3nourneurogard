package com.neuroguard.assuranceservice.service;

import com.neuroguard.assuranceservice.entity.Notification;
import com.neuroguard.assuranceservice.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@Slf4j
public class EmailNotificationService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired(required = false)
    private TemplateEngine templateEngine;

    @Autowired
    private NotificationRepository notificationRepository;

    public void sendEmailNotification(Long patientId, String email, String type,
                                     String subject, Map<String, Object> variables) {
        log.info("📧 Attempting to send email to: {}", email);
        log.info("   Subject: {}", subject);
        log.info("   Type: {}", type);

        if (mailSender == null) {
            log.error("❌ JavaMailSender is NULL - not configured!");
            return;
        }

        try {
            // Render email template with Thymeleaf if available
            String htmlContent = renderTemplate(type, variables);

            // Send HTML email using MimeMessage
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setFrom("noreply@neuroguard.com");
            helper.setTo(email);
            helper.setSubject(subject);

            // Set HTML content if available, otherwise use plain text
            if (htmlContent != null && !htmlContent.isEmpty()) {
                helper.setText(htmlContent, true); // true = HTML content
                log.info("   Sending HTML email via JavaMailSender...");
            } else {
                String plainText = buildPlainTextMessage(type, variables);
                helper.setText(plainText, false); // false = plain text
                log.info("   Sending plain text email via JavaMailSender...");
            }

            mailSender.send(mimeMessage);

            // Save to history
            saveNotification(patientId, email, type, "EMAIL", subject, htmlContent != null ? htmlContent : buildPlainTextMessage(type, variables), "SENT", null);
            log.info("✅ Email sent successfully to {}", email);

        } catch (Exception e) {
            log.error("❌ Error sending email to {}: {} - {}", email, e.getClass().getName(), e.getMessage(), e);
            saveNotification(patientId, email, type, "EMAIL", subject, "", "FAILED", e.getMessage());
        }
    }

    private String renderTemplate(String type, Map<String, Object> variables) {
        if (templateEngine == null) {
            return null;
        }

        try {
            Context context = new Context();
            context.setVariable("now", LocalDateTime.now());
            if (variables != null) {
                context.setVariables(variables);
            }

            String templateName = "email-" + type.toLowerCase().replace("_", "-");
            return templateEngine.process(templateName, context);
        } catch (Exception e) {
            log.warn("Could not render template for type {}: {}", type, e.getMessage());
            return null;
        }
    }

    private String buildPlainTextMessage(String type, Map<String, Object> variables) {
        if (variables == null) {
            return "Notification from NeuroGuard";
        }

        switch (type) {
            case "ASSURANCE_CREATED":
                return String.format("Dear %s,\n\nYour insurance application has been received.\n" +
                                "Provider: %s\nPolicy #: %s\nStatus: PENDING\n\n" +
                                "Best regards,\nNeuroGuard Team",
                        variables.get("patientName"), variables.get("providerName"), variables.get("policyNumber"));

            case "ASSURANCE_APPROVED":
                return String.format("Dear %s,\n\nYour insurance has been APPROVED!\n" +
                                "Provider: %s\nPolicy #: %s\n\n" +
                                "Best regards,\nNeuroGuard Team",
                        variables.get("patientName"), variables.get("providerName"), variables.get("policyNumber"));

            case "ASSURANCE_REJECTED":
                return String.format("Dear %s,\n\nUnfortunately, your insurance application has been REJECTED.\n" +
                                "Please contact us for more information.\n\n" +
                                "Best regards,\nNeuroGuard Team",
                        variables.get("patientName"));

            default:
                return "Notification from NeuroGuard";
        }
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
            log.error("Failed to save notification history: {}", e.getMessage());
        }
    }
}
