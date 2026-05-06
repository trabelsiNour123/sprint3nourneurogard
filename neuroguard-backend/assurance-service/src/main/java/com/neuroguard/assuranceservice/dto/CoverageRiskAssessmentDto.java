package com.neuroguard.assuranceservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoverageRiskAssessmentDto {
    private Long id;
    private Long assuranceId;
    private Long patientId;

    // ML-Derived Risk Metrics
    private Double alzheimersPredictionScore;
    private String alzheimersPredictionLevel;

    // Clinical Profile Metrics
    private Integer activeAlertCount;
    private String highestAlertSeverity;
    private Double alertSeverityRatio;

    // Medical Complexity Score
    private Integer medicalComplexityScore;

    // Coverage Recommendations
    private String recommendedCoverageLevel;
    private Double estimatedAnnualClaimCost;
    private List<String> recommendedProcedures;

    // Care Coordination
    private Integer recommendedProviderCount;
    private Boolean neurologyReferralNeeded;
    private Boolean geriatricAssessmentNeeded;

    // Assessment Dates
    private LocalDateTime lastAssessmentDate;
    private LocalDateTime nextRecommendedAssessmentDate;

    // Risk Stratification
    private String riskStratum;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
