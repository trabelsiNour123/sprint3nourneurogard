package com.neuroguard.assuranceservice.repository;

import com.neuroguard.assuranceservice.entity.CoverageRiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CoverageRiskAssessmentRepository extends JpaRepository<CoverageRiskAssessment, Long> {
    Optional<CoverageRiskAssessment> findByAssuranceId(Long assuranceId);
    Optional<CoverageRiskAssessment> findByPatientId(Long patientId);
    void deleteByAssuranceId(Long assuranceId);
}
