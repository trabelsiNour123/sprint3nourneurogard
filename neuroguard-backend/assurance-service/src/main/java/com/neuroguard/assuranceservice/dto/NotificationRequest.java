package com.neuroguard.assuranceservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {

    private Long patientId;
    private String email;
    private String phoneNumber;
    private String type; // ASSURANCE_CREATED, ASSURANCE_APPROVED, ASSURANCE_REJECTED
    private String subject;
    private Map<String, Object> templateVariables;
    private List<String> channels; // EMAIL, SMS

    public NotificationRequest(Long patientId, String email, String phoneNumber,
                              String type, String subject, Map<String, Object> variables) {
        this.patientId = patientId;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.type = type;
        this.subject = subject;
        this.templateVariables = variables;
        this.channels = java.util.Arrays.asList("EMAIL"); // Default to email only
    }
}
