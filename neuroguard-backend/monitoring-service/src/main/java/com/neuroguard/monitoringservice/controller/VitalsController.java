package com.neuroguard.monitoringservice.controller;

import com.neuroguard.monitoringservice.entity.VitalsEntity;
import com.neuroguard.monitoringservice.repository.VitalsRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import org.springframework.web.bind.annotation.CrossOrigin;


@RestController
@RequestMapping("/vitals")
public class VitalsController {

    private final VitalsRepository vitalsRepository;

    public VitalsController(VitalsRepository vitalsRepository) {
        this.vitalsRepository = vitalsRepository;
    }

    @GetMapping("/{patientId}/latest")
    public ResponseEntity<?> getLatestVitals(@PathVariable String patientId) {
        Optional<VitalsEntity> vitalsOptional = vitalsRepository.findTopByPatientIdOrderByTimestampDesc(patientId);
        
        if (vitalsOptional.isPresent()) {
            return ResponseEntity.ok(vitalsOptional.get());
        } else {
            // Return a clearer message rather than a raw 404 so we don't trigger a Whitelabel Error Page
            return ResponseEntity.status(404).body("{\"message\": \"No vitals found yet for this patient. Please wait at least 10 seconds for the simulator to trigger!\"}");
        }
    }
}
