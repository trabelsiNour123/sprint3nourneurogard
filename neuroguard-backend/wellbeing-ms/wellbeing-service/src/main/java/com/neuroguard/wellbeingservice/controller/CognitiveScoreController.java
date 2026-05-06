package com.neuroguard.wellbeingservice.controller;

import com.neuroguard.wellbeingservice.entity.CognitiveScore;
import com.neuroguard.wellbeingservice.service.CognitiveScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wellbeing/cognitive-games")

public class CognitiveScoreController {

    @Autowired
    private CognitiveScoreService service;

    @PostMapping("/result")
    public ResponseEntity<CognitiveScore> saveResult(@RequestBody CognitiveScore score) {
        return ResponseEntity.ok(service.saveResult(score));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<CognitiveScore>> getPatientResults(@PathVariable String patientId) {
        return ResponseEntity.ok(service.getPatientResults(patientId));
    }

    @GetMapping("/patient/{patientId}/game/{gameType}")
    public ResponseEntity<List<CognitiveScore>> getPatientResultsByGame(
            @PathVariable String patientId, 
            @PathVariable String gameType) {
        return ResponseEntity.ok(service.getPatientResultsByGame(patientId, gameType));
    }
}
