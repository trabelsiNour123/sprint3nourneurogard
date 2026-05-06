package com.neuroguard.assuranceservice.util;

import com.neuroguard.assuranceservice.dto.CoverageRiskAssessmentDto;
import com.neuroguard.assuranceservice.entity.CoverageRiskAssessment;
import org.springframework.stereotype.Component;

@Component
public class CoverageRiskAssessmentMapper {

    /**
     * Convert entity to DTO
     */
    public CoverageRiskAssessmentDto toDto(CoverageRiskAssessment entity) {
        if (entity == null) {
            return null;
        }

        CoverageRiskAssessmentDto dto = new CoverageRiskAssessmentDto();
        dto.setId(entity.getId());
        dto.setAssuranceId(entity.getAssuranceId());
        dto.setPatientId(entity.getPatientId());
        dto.setAlzheimersPredictionScore(entity.getAlzheimersPredictionScore());
        dto.setAlzheimersPredictionLevel(entity.getAlzheimersPredictionLevel());
        dto.setActiveAlertCount(entity.getActiveAlertCount());
        dto.setHighestAlertSeverity(entity.getHighestAlertSeverity());
        dto.setAlertSeverityRatio(entity.getAlertSeverityRatio());
        dto.setMedicalComplexityScore(entity.getMedicalComplexityScore());
        dto.setRecommendedCoverageLevel(entity.getRecommendedCoverageLevel());
        dto.setEstimatedAnnualClaimCost(entity.getEstimatedAnnualClaimCost());
        dto.setRecommendedProcedures(entity.getRecommendedProcedures());
        dto.setRecommendedProviderCount(entity.getRecommendedProviderCount());
        dto.setNeurologyReferralNeeded(entity.getNeurologyReferralNeeded());
        dto.setGeriatricAssessmentNeeded(entity.getGeriatricAssessmentNeeded());
        dto.setLastAssessmentDate(entity.getLastAssessmentDate());
        dto.setNextRecommendedAssessmentDate(entity.getNextRecommendedAssessmentDate());
        dto.setRiskStratum(entity.getRiskStratum());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    /**
     * Convert DTO to entity (for creation)
     */
    public CoverageRiskAssessment toEntity(CoverageRiskAssessmentDto dto) {
        if (dto == null) {
            return null;
        }

        CoverageRiskAssessment entity = new CoverageRiskAssessment();
        entity.setId(dto.getId());
        entity.setAssuranceId(dto.getAssuranceId());
        entity.setPatientId(dto.getPatientId());
        entity.setAlzheimersPredictionScore(dto.getAlzheimersPredictionScore());
        entity.setAlzheimersPredictionLevel(dto.getAlzheimersPredictionLevel());
        entity.setActiveAlertCount(dto.getActiveAlertCount());
        entity.setHighestAlertSeverity(dto.getHighestAlertSeverity());
        entity.setAlertSeverityRatio(dto.getAlertSeverityRatio());
        entity.setMedicalComplexityScore(dto.getMedicalComplexityScore());
        entity.setRecommendedCoverageLevel(dto.getRecommendedCoverageLevel());
        entity.setEstimatedAnnualClaimCost(dto.getEstimatedAnnualClaimCost());
        entity.setRecommendedProcedures(dto.getRecommendedProcedures());
        entity.setRecommendedProviderCount(dto.getRecommendedProviderCount());
        entity.setNeurologyReferralNeeded(dto.getNeurologyReferralNeeded());
        entity.setGeriatricAssessmentNeeded(dto.getGeriatricAssessmentNeeded());
        entity.setLastAssessmentDate(dto.getLastAssessmentDate());
        entity.setNextRecommendedAssessmentDate(dto.getNextRecommendedAssessmentDate());
        entity.setRiskStratum(dto.getRiskStratum());
        return entity;
    }
}
