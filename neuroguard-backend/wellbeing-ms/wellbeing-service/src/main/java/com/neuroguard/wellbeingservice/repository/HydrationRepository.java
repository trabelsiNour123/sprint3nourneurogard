package com.neuroguard.wellbeingservice.repository;

import com.neuroguard.wellbeingservice.entity.Hydration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface HydrationRepository extends JpaRepository<Hydration, Long> {
    Optional<Hydration> findByUserIdAndDate(String userId, LocalDate date);
}
