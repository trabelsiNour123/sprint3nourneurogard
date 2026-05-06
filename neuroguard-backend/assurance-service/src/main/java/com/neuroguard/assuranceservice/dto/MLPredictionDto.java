package com.neuroguard.assuranceservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MLPredictionDto {
    private String patientId;
    private Double probability;                   // 0.0-1.0
    private String riskLevel;                     // MINIMAL|LOW|MODERATE|HIGH|CRITICAL
    private String explanation;
    private String recommendations;
}
