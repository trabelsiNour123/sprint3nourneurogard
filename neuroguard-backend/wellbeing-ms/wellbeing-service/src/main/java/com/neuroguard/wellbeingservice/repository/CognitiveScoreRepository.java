package com.neuroguard.wellbeingservice.repository;

import com.neuroguard.wellbeingservice.entity.CognitiveScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CognitiveScoreRepository extends JpaRepository<CognitiveScore, Long> {
    List<CognitiveScore> findByPatientIdOrderByTimestampDesc(String patientId);
    List<CognitiveScore> findByPatientIdAndGameTypeOrderByTimestampDesc(String patientId, CognitiveScore.GameType gameType);
}
