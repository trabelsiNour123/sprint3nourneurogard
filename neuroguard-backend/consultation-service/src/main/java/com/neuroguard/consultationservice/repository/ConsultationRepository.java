package com.neuroguard.consultationservice.repository;

import com.neuroguard.consultationservice.entity.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
    List<Consultation> findByProviderId(Long providerId);
    List<Consultation> findByPatientId(Long patientId);
    List<Consultation> findByCaregiverId(Long caregiverId);

    @Query("SELECT c FROM Consultation c WHERE c.providerId = :providerId AND c.status != 'CANCELLED' " +
           "AND c.startTime < :endTime AND (c.endTime IS NULL OR c.endTime > :startTime)")
    List<Consultation> findOverlappingConsultations(@Param("providerId") Long providerId,
                                                    @Param("startTime") LocalDateTime startTime,
                                                    @Param("endTime") LocalDateTime endTime);

    @Query("SELECT c.status, COUNT(c) FROM Consultation c GROUP BY c.status")
    List<Object[]> countByStatus();

    @Query("SELECT c.type, COUNT(c) FROM Consultation c GROUP BY c.type")
    List<Object[]> countByType();

    @Query("SELECT c.status, COUNT(c) FROM Consultation c WHERE c.providerId = :providerId GROUP BY c.status")
    List<Object[]> countByStatusAndProviderId(@Param("providerId") Long providerId);

    @Query("SELECT c.type, COUNT(c) FROM Consultation c WHERE c.providerId = :providerId GROUP BY c.type")
    List<Object[]> countByTypeAndProviderId(@Param("providerId") Long providerId);

    @Query("SELECT FUNCTION('MONTHNAME', c.startTime), COUNT(c) FROM Consultation c " +
           "WHERE FUNCTION('YEAR', c.startTime) = :year GROUP BY FUNCTION('MONTH', c.startTime), FUNCTION('MONTHNAME', c.startTime) " +
           "ORDER BY FUNCTION('MONTH', c.startTime)")
    List<Object[]> countByMonthName(@Param("year") int year);

    @Query("SELECT c.patientId, COUNT(c) FROM Consultation c GROUP BY c.patientId ORDER BY COUNT(c) DESC")
    List<Object[]> findTopPatientIds();

    @Query("SELECT c.patientId, COUNT(c) FROM Consultation c WHERE c.providerId = :providerId GROUP BY c.patientId ORDER BY COUNT(c) DESC")
    List<Object[]> findTopPatientIdsByProviderId(@Param("providerId") Long providerId);
}