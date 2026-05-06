package com.neuroguard.assuranceservice.scheduler;

import com.neuroguard.assuranceservice.entity.Assurance;
import com.neuroguard.assuranceservice.repository.AssuranceRepository;
import com.neuroguard.assuranceservice.service.CoverageRiskAssessmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RiskAssessmentScheduler {

    private final AssuranceRepository assuranceRepository;
    private final CoverageRiskAssessmentService coverageRiskAssessmentService;

    /**
     * Automatically recalculate risk assessments for assurances
     * scheduled to be reassessed based on their next assessment date
     * 
     * Runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void recalculateDueAssessments() {
        log.info("Starting scheduled risk assessment recalculation...");

        try {
            LocalDateTime now = LocalDateTime.now();
            List<Assurance> allAssurances = assuranceRepository.findAll();

            int recalculatedCount = 0;

            for (Assurance assurance : allAssurances) {
                if (assurance.getCoverageRiskAssessment() != null) {
                    LocalDateTime nextAssessmentDate = assurance.getCoverageRiskAssessment().getNextRecommendedAssessmentDate();

                    if (nextAssessmentDate != null && now.isAfter(nextAssessmentDate)) {
                        log.debug("Recalculating assessment for assurance ID: {}", assurance.getId());

                        try {
                            coverageRiskAssessmentService.recalculateRiskAssessment(
                                    assurance.getId(),
                                    assurance.getPatientId()
                            );
                            recalculatedCount++;
                        } catch (Exception e) {
                            log.error("Error recalculating assessment for assurance ID: {}", assurance.getId(), e);
                        }
                    }
                }
            }

            log.info("Completed scheduled risk assessment recalculation. Updated {} assessments.", recalculatedCount);

        } catch (Exception e) {
            log.error("Error in scheduled risk assessment recalculation", e);
        }
    }

    /**
     * Monitor high-risk assessments and generate alerts
     * Runs every 6 hours
     */
    @Scheduled(cron = "0 0 */6 * * *")
    public void monitorHighRiskAssessments() {
        log.info("Starting high-risk assessment monitoring...");

        try {
            List<Assurance> allAssurances = assuranceRepository.findAll();

            int criticalCount = 0;
            int highCount = 0;

            for (Assurance assurance : allAssurances) {
                if (assurance.getCoverageRiskAssessment() != null) {
                    Integer complexityScore = assurance.getCoverageRiskAssessment().getMedicalComplexityScore();

                    if (complexityScore >= 85) {
                        criticalCount++;
                        log.warn("CRITICAL RISK: Assurance ID {} has complexity score {}", assurance.getId(), complexityScore);
                    } else if (complexityScore >= 65) {
                        highCount++;
                        log.info("HIGH RISK: Assurance ID {} has complexity score {}", assurance.getId(), complexityScore);
                    }
                }
            }

            log.info("Risk monitoring complete. Critical: {}, High: {}", criticalCount, highCount);

        } catch (Exception e) {
            log.error("Error in high-risk assessment monitoring", e);
        }
    }
}
