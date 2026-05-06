package com.neuroguard.assuranceservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertDto {
    private Long id;
    private Long patientId;
    private String message;
    private String severity;                      // INFO|WARNING|CRITICAL
    private String riskLevel;                     // MINIMAL|LOW|MODERATE|HIGH|CRITICAL
    private Boolean resolved;
    private Long createdBy;
    private LocalDateTime createdAt;
}
