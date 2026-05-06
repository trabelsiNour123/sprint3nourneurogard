package com.neuroguard.riskalertservice.controller;

import com.neuroguard.riskalertservice.dto.AlertResponse;
import com.neuroguard.riskalertservice.service.AlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Alert Batch Endpoints - Extended Alert Controller for batch operations
 * Supports fetching alerts for multiple patients in a single request
 */
@RestController
@RequestMapping("/alerts")
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class AlertBatchController {

    @Autowired
    private AlertService alertService;

    /**
     * Get alerts for a specific patient
     * Endpoint: GET /alerts/patient/{patientId}
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AlertResponse>> getPatientAlerts(@PathVariable Long patientId) {
        log.info("Fetching alerts for patient: {}", patientId);
        List<AlertResponse> alerts = alertService.getAlertsByPatientId(patientId);
        return ResponseEntity.ok(alerts);
    }

    /**
     * Get alerts for multiple patients in batch
     * Endpoint: POST /alerts/batch
     *
     * @param patientIds List of patient IDs
     * @return List of alerts for all specified patients
     */
    @PostMapping("/batch")
    public ResponseEntity<List<AlertResponse>> getAlertsBatch(@RequestBody List<Long> patientIds) {
        log.info("Fetching alerts in batch for {} patients", patientIds.size());

        List<AlertResponse> allAlerts = patientIds.stream()
            .flatMap(patientId -> alertService.getAlertsByPatientId(patientId).stream())
            .collect(Collectors.toList());

        log.info("Retrieved {} alerts for batch request", allAlerts.size());
        return ResponseEntity.ok(allAlerts);
    }

    /**
     * Get unresolved alerts for a patient
     * Endpoint: GET /alerts/patient/{patientId}/unresolved
     */
    @GetMapping("/patient/{patientId}/unresolved")
    public ResponseEntity<List<AlertResponse>> getUnresolvedAlerts(@PathVariable Long patientId) {
        log.info("Fetching unresolved alerts for patient: {}", patientId);
        List<AlertResponse> alerts = alertService.getAlertsByPatientId(patientId).stream()
            .filter(alert -> !alert.isResolved())
            .collect(Collectors.toList());
        return ResponseEntity.ok(alerts);
    }

    /**
     * Get critical alerts for a patient
     * Endpoint: GET /alerts/patient/{patientId}/critical
     */
    @GetMapping("/patient/{patientId}/critical")
    public ResponseEntity<List<AlertResponse>> getCriticalAlerts(@PathVariable Long patientId) {
        log.info("Fetching critical alerts for patient: {}", patientId);
        List<AlertResponse> alerts = alertService.getAlertsByPatientId(patientId).stream()
            .filter(alert -> "CRITICAL".equalsIgnoreCase(alert.getSeverity()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(alerts);
    }

    /**
     * Get unresolved critical alerts for a patient
     * Endpoint: GET /alerts/patient/{patientId}/critical/unresolved
     */
    @GetMapping("/patient/{patientId}/critical/unresolved")
    public ResponseEntity<List<AlertResponse>> getUnresolvedCriticalAlerts(@PathVariable Long patientId) {
        log.info("Fetching unresolved critical alerts for patient: {}", patientId);
        List<AlertResponse> alerts = alertService.getAlertsByPatientId(patientId).stream()
            .filter(alert -> !alert.isResolved() && "CRITICAL".equalsIgnoreCase(alert.getSeverity()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(alerts);
    }
}
