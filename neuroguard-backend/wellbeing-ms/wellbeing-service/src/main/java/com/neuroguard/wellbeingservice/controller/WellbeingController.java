package com.neuroguard.wellbeingservice.controller;

import com.neuroguard.wellbeingservice.dto.PatientPulseDTO;
import com.neuroguard.wellbeingservice.entity.Hydration;
import com.neuroguard.wellbeingservice.entity.Mood;
import com.neuroguard.wellbeingservice.entity.Sleep;
import com.neuroguard.wellbeingservice.service.WellbeingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/wellbeing")
public class WellbeingController {

    private final WellbeingService wellbeingService;

    public WellbeingController(WellbeingService wellbeingService) {
        this.wellbeingService = wellbeingService;
    }

    @PostMapping("/mood")
    public ResponseEntity<Mood> saveMood(@RequestBody Mood mood) {
        return ResponseEntity.ok(wellbeingService.saveMood(mood));
    }

    @GetMapping("/mood/{userId}/trends")
    public ResponseEntity<List<Mood>> getMoodTrends(@PathVariable String userId) {
        return ResponseEntity.ok(wellbeingService.getMoodTrends(userId));
    }

    @PostMapping("/sleep")
    public ResponseEntity<Sleep> logSleep(@RequestBody Sleep sleep) {
        return ResponseEntity.ok(wellbeingService.logSleep(sleep));
    }

    @GetMapping("/sleep/{userId}/avg")
    public ResponseEntity<Double> getAverageSleep(@PathVariable String userId) {
        return ResponseEntity.ok(wellbeingService.getAverageSleep(userId));
    }

    @PatchMapping("/hydration/{userId}/add")
    public ResponseEntity<Hydration> addHydration(@PathVariable String userId) {
        return ResponseEntity.ok(wellbeingService.addHydration(userId));
    }

    @GetMapping("/pulse/{userId}")
    public ResponseEntity<PatientPulseDTO> getPulse(@PathVariable String userId) {
        return ResponseEntity.ok(wellbeingService.getPatientPulse(userId));
    }

    @GetMapping("/hydration/{userId}/today")
    public ResponseEntity<Hydration> getTodayHydration(@PathVariable String userId) {
        return ResponseEntity.ok(wellbeingService.getTodayHydration(userId));
    }

    @PatchMapping("/hydration/{userId}/reset")
    public ResponseEntity<Hydration> resetHydration(@PathVariable String userId) {
        return ResponseEntity.ok(wellbeingService.resetHydration(userId));
    }
    @GetMapping("/mood/{userId}/latest")
    public ResponseEntity<Mood> getLatestMood(@PathVariable String userId) {
        // You can implement this in your service to return the single most recent mood record
        return ResponseEntity.ok(wellbeingService.getLatestMood(userId));
    }

    @GetMapping("/sleep/{userId}/latest")
    public ResponseEntity<Sleep> getLatestSleep(@PathVariable String userId) {
        Sleep latestSleep = wellbeingService.getLatestSleep(userId);
        if (latestSleep != null) {
            return ResponseEntity.ok(latestSleep);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
