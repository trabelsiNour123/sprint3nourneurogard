package com.neuroguard.wellbeingservice.repository;

import com.neuroguard.wellbeingservice.entity.Mood;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MoodRepository extends JpaRepository<Mood, Long> {
    List<Mood> findByUserIdOrderByTimestampDesc(String userId, Pageable pageable);

    Optional<Mood> findFirstByUserIdOrderByTimestampDesc(String userId);
}
