package com.neuroguard.riskalertservice.repository;

import com.neuroguard.riskalertservice.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Alert Statistics Repository - Query aggregations for alert data
 * Provides statistics across patients, severity levels, and resolution status
 */
public interface AlertStatisticsRepository extends JpaRepository<Alert, Long> {

    /**
     * Count alerts by severity for a patient
     */
    @Query("""
        SELECT COUNT(a)
        FROM Alert a
        WHERE a.patientId = :patientId
        AND a.severity = :severity
        """)
    int countBySeverity(@Param("patientId") Long patientId, @Param("severity") String severity);

    /**
     * Count unresolved alerts for a patient
     */
    @Query("""
        SELECT COUNT(a)
        FROM Alert a
        WHERE a.patientId = :patientId
        AND a.resolved = false
        """)
    int countUnresolvedByPatientId(@Param("patientId") Long patientId);

    /**
     * Count resolved alerts for a patient
     */
    @Query("""
        SELECT COUNT(a)
        FROM Alert a
        WHERE a.patientId = :patientId
        AND a.resolved = true
        """)
    int countResolvedByPatientId(@Param("patientId") Long patientId);

    /**
     * Count total alerts for a patient
     */
    @Query("""
        SELECT COUNT(a)
        FROM Alert a
        WHERE a.patientId = :patientId
        """)
    int countByPatientId(@Param("patientId") Long patientId);

    /**
     * Count critical unresolved alerts for a patient
     */
    @Query("""
        SELECT COUNT(a)
        FROM Alert a
        WHERE a.patientId = :patientId
        AND a.severity = 'CRITICAL'
        AND a.resolved = false
        """)
    int countCriticalUnresolved(@Param("patientId") Long patientId);

    /**
     * Count alerts by severity for multiple patients (for caregiver/provider)
     */
    @Query("""
        SELECT COUNT(a)
        FROM Alert a
        WHERE a.patientId IN :patientIds
        AND a.severity = :severity
        """)
    int countBySeverityForPatients(@Param("patientIds") java.util.List<Long> patientIds, @Param("severity") String severity);

    /**
     * Count unresolved alerts for multiple patients
     */
    @Query("""
        SELECT COUNT(a)
        FROM Alert a
        WHERE a.patientId IN :patientIds
        AND a.resolved = false
        """)
    int countUnresolvedForPatients(@Param("patientIds") java.util.List<Long> patientIds);

    /**
     * Count critical unresolved alerts for multiple patients
     */
    @Query("""
        SELECT COUNT(a)
        FROM Alert a
        WHERE a.patientId IN :patientIds
        AND a.severity = 'CRITICAL'
        AND a.resolved = false
        """)
    int countCriticalUnresolvedForPatients(@Param("patientIds") java.util.List<Long> patientIds);

    /**
     * Count total alerts for multiple patients
     */
    @Query("""
        SELECT COUNT(a)
        FROM Alert a
        WHERE a.patientId IN :patientIds
        """)
    int countForPatients(@Param("patientIds") java.util.List<Long> patientIds);

    /**
     * Get alert resolution rate (%)
     */
    @Query("""
        SELECT (CAST(COUNT(CASE WHEN a.resolved = true THEN 1 END) AS double)
                / NULLIF(COUNT(a), 0) * 100)
        FROM Alert a
        WHERE a.patientId = :patientId
        """)
    double getResolutionRate(@Param("patientId") Long patientId);

    /**
     * Get alert resolution rate for multiple patients
     */
    @Query("""
        SELECT (CAST(COUNT(CASE WHEN a.resolved = true THEN 1 END) AS double)
                / NULLIF(COUNT(a), 0) * 100)
        FROM Alert a
        WHERE a.patientId IN :patientIds
        """)
    double getResolutionRateForPatients(@Param("patientIds") java.util.List<Long> patientIds);

    /**
     * Get critical alert percentage for a patient
     */
    @Query("""
        SELECT (CAST(COUNT(CASE WHEN a.severity = 'CRITICAL' THEN 1 END) AS double)
                / NULLIF(COUNT(a), 0) * 100)
        FROM Alert a
        WHERE a.patientId = :patientId
        """)
    double getCriticalAlertPercentage(@Param("patientId") Long patientId);
}
