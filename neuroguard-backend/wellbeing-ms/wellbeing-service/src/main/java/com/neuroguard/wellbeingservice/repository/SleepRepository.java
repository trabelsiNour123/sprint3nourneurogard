package com.neuroguard.wellbeingservice.repository;

import com.neuroguard.wellbeingservice.entity.Sleep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SleepRepository extends JpaRepository<Sleep, Long> {
    Optional<Sleep> findTopByUserIdOrderByDateDesc(String userId);
}
