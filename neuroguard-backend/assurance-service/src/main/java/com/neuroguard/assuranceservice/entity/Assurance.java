package com.neuroguard.assuranceservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Assurance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long patientId;

    @Column(nullable = false)
    private String providerName;

    @Column(nullable = false)
    private String policyNumber;

    @Column(columnDefinition = "TEXT")
    private String coverageDetails;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String illness;

    @Column(nullable = false, length = 10)
    private String postalCode;

    @Column(nullable = false)
    private String mobilePhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssuranceStatus status = AssuranceStatus.PENDING;

    // Coverage Risk Assessment Fields
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "coverage_risk_assessment_id", unique = true)
    private CoverageRiskAssessment coverageRiskAssessment;

    @Column(length = 50)
    private String recommendedCoverageLevel;

    private Double estimatedAnnualClaimCost;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
