package com.neuroguard.assuranceservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "coverage_risk_assessments")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CoverageRiskAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long assuranceId;

    @Column(nullable = false)
    private Long patientId;

    // ML-Derived Risk Metrics
    private Double alzheimersPredictionScore;      // 0.0-1.0 from ML service
    private String alzheimersPredictionLevel;       // MINIMAL|LOW|MODERATE|HIGH|CRITICAL

    // Clinical Profile Metrics
    private Integer activeAlertCount;               // Count of unresolved alerts
    private String highestAlertSeverity;            // CRITICAL|WARNING|INFO
    private Double alertSeverityRatio;              // (CRITICAL+WARNING) / total

    // Medical Complexity Score (0-100)
    @Column(nullable = false)
    private Integer medicalComplexityScore;

    // Coverage Recommendations
    private String recommendedCoverageLevel;        // BASIC|ENHANCED|COMPREHENSIVE|INTENSIVE
    private Double estimatedAnnualClaimCost;        // Predicted claims amount in USD

    @ElementCollection
    @CollectionTable(name = "assessment_recommended_procedures", joinColumns = @JoinColumn(name = "assessment_id"))
    @Column(name = "procedure_name")
    private List<String> recommendedProcedures = new ArrayList<>();

    // Care Coordination Indicators
    private Integer recommendedProviderCount;       // Optimal care team size
    private Boolean neurologyReferralNeeded;
    private Boolean geriatricAssessmentNeeded;

    // Reassessment Triggers
    @Column(name = "last_assessment_date")
    private LocalDateTime lastAssessmentDate;

    @Column(name = "next_recommended_assessment_date")
    private LocalDateTime nextRecommendedAssessmentDate;

    // Risk Stratification
    private String riskStratum;                     // VERY_LOW|LOW|MODERATE|HIGH|VERY_HIGH

    // Timestamps
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
