package com.neuroguard.assuranceservice.repository;

import com.neuroguard.assuranceservice.entity.Assurance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssuranceRepository extends JpaRepository<Assurance, Long> {
    List<Assurance> findByPatientId(Long patientId);
}
