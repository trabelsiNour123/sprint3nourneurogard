package com.neuroguard.monitoringservice.repository;

import com.neuroguard.monitoringservice.entity.VitalsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VitalsRepository extends JpaRepository<VitalsEntity, Long> {
    Optional<VitalsEntity> findTopByPatientIdOrderByTimestampDesc(String patientId);
    java.util.List<VitalsEntity> findByPatientId(String patientId);
}
