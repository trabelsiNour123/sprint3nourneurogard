package com.neuroguard.wellbeingservice.service;

import com.neuroguard.wellbeingservice.entity.CognitiveScore;
import com.neuroguard.wellbeingservice.repository.CognitiveScoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CognitiveScoreService {

    @Autowired
    private CognitiveScoreRepository repository;

    public CognitiveScore saveResult(CognitiveScore score) {
        return repository.save(score);
    }

    public List<CognitiveScore> getPatientResults(String patientId) {
        return repository.findByPatientIdOrderByTimestampDesc(patientId);
    }

    public List<CognitiveScore> getPatientResultsByGame(String patientId, String gameType) {
        return repository.findByPatientIdAndGameTypeOrderByTimestampDesc(
            patientId, 
            CognitiveScore.GameType.valueOf(gameType.toUpperCase())
        );
    }
}
