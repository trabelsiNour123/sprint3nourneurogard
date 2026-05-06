package com.neuroguard.monitoringservice.controller;

import com.neuroguard.monitoringservice.entity.SleepEntity;
import com.neuroguard.monitoringservice.service.SleepService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;


@RestController
@RequestMapping("")
public class MonitoringController {

    private final SleepService sleepService; 
    private final com.neuroguard.monitoringservice.service.TaskService taskService;
    private final com.neuroguard.monitoringservice.service.ReportGenerationService reportGenerationService;

    public MonitoringController(SleepService sleepService,
            com.neuroguard.monitoringservice.service.TaskService taskService,
            com.neuroguard.monitoringservice.service.ReportGenerationService reportGenerationService) {
        this.sleepService = sleepService;
        this.taskService = taskService;
        this.reportGenerationService = reportGenerationService;
    }

    @PostMapping("/sleep")
    public ResponseEntity<SleepEntity> logSleep(@RequestBody SleepEntity sleepEntity) {
        SleepEntity savedSleep = sleepService.logSleep(sleepEntity);
        return new ResponseEntity<>(savedSleep, HttpStatus.CREATED);
    }

    @GetMapping("/sleep/patient/{patientId}/logged-today")
    public ResponseEntity<Boolean> hasSleepLoggedToday(@PathVariable String patientId) {
        return ResponseEntity.ok(sleepService.hasSleepLoggedToday(patientId));
    }


    // --- Task Management: Patient Flow Endpoints ---

    @GetMapping("/tasks/patient/{patientId}")
    public ResponseEntity<List<com.neuroguard.monitoringservice.entity.TaskEntity>> getPatientTasks(
            @PathVariable String patientId) {
        return ResponseEntity.ok(taskService.getTasks(patientId, "PATIENT"));
    }

    @PostMapping("/tasks/patient/{patientId}")
    public ResponseEntity<com.neuroguard.monitoringservice.entity.TaskEntity> createPatientTask(
            @PathVariable String patientId,
            @RequestBody com.neuroguard.monitoringservice.entity.TaskEntity taskEntity) {
        return new ResponseEntity<>(taskService.createTask(patientId, "PATIENT", taskEntity), HttpStatus.CREATED);
    }

    @PatchMapping("/tasks/patient/task/{taskId}/toggle")
    public ResponseEntity<com.neuroguard.monitoringservice.entity.TaskEntity> togglePatientTask(
            @PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.toggleTaskDone(taskId));
    }

    @DeleteMapping("/tasks/patient/task/{taskId}")
    public ResponseEntity<Void> deletePatientTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    // --- Task Management: Caregiver Flow Endpoints ---

    @GetMapping("/tasks/caregiver/{caregiverId}")
    public ResponseEntity<List<com.neuroguard.monitoringservice.entity.TaskEntity>> getCaregiverTasks(
            @PathVariable String caregiverId) {
        return ResponseEntity.ok(taskService.getTasks(caregiverId, "CAREGIVER"));
    }

    @PostMapping("/tasks/caregiver/{caregiverId}")
    public ResponseEntity<com.neuroguard.monitoringservice.entity.TaskEntity> createCaregiverTask(
            @PathVariable String caregiverId,
            @RequestBody com.neuroguard.monitoringservice.entity.TaskEntity taskEntity) {
        return new ResponseEntity<>(taskService.createTask(caregiverId, "CAREGIVER", taskEntity), HttpStatus.CREATED);
    }

    @PatchMapping("/tasks/caregiver/task/{taskId}/toggle")
    public ResponseEntity<com.neuroguard.monitoringservice.entity.TaskEntity> toggleCaregiverTask(
            @PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.toggleTaskDone(taskId));
    }

    @DeleteMapping("/tasks/caregiver/task/{taskId}")
    public ResponseEntity<Void> deleteCaregiverTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    // Future Proofing (Interfaces only)

    @GetMapping("/cognitive/{patientId}/assessments")
    public ResponseEntity<List<Object>> getCognitiveAssessments(@PathVariable String patientId) {
        // Placeholder for cognitive assessments
        return ResponseEntity.ok(Collections.emptyList());
    }

    @GetMapping("/alerts/{patientId}/active")
    public ResponseEntity<List<Object>> getActiveAlerts(@PathVariable String patientId) {
        // Placeholder for active alerts
        return ResponseEntity.ok(Collections.emptyList());
    }

    @GetMapping("/sleep/patient/{patientId}")
    public ResponseEntity<List<SleepEntity>> getSleepHistory(@PathVariable String patientId) {
        return ResponseEntity.ok(sleepService.getSleepHistory(patientId));
    }

    @GetMapping("/sleep/{userId}/latest")
    public ResponseEntity<SleepEntity> getLatestSleep(@PathVariable String userId) {
        SleepEntity latestSleep = sleepService.getLatestSleep(userId);
        if (latestSleep != null) {
            return ResponseEntity.ok(latestSleep);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // --- Report Generation ---
    @PostMapping("/reports/generate/{patientId}")
    public ResponseEntity<String> triggerReportGeneration(@PathVariable String patientId) {
        
        try {
            reportGenerationService.generateManualReport(patientId);
            return ResponseEntity.ok("Successfully generated and queued report for patient " + patientId);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating report: " + e.getMessage());
        }
    }

}
