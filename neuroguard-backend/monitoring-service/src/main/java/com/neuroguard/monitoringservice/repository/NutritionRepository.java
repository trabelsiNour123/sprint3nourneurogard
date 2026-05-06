package com.neuroguard.monitoringservice.repository;

import com.neuroguard.monitoringservice.entity.NutritionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface NutritionRepository extends JpaRepository<NutritionEntity, Long> {
    Optional<NutritionEntity> findByPatientIdAndDate(String patientId, LocalDate date);
    Optional<NutritionEntity> findTopByPatientIdOrderByDateDesc(String patientId);
}
