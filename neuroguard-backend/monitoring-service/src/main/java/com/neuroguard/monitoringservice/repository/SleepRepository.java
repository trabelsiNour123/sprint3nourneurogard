package com.neuroguard.monitoringservice.repository;

import com.neuroguard.monitoringservice.entity.SleepEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SleepRepository extends JpaRepository<SleepEntity, Long> {
    List<SleepEntity> findByPatientId(String patientId);

    List<SleepEntity> findAllByPatientIdOrderByTimestampDesc(String patientId);

    Optional<SleepEntity> findFirstByPatientIdAndTimestampBetween(
            String patientId, LocalDateTime startOfDay, LocalDateTime endOfDay);
}
