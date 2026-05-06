package com.neuroguard.assuranceservice.controller;

import com.neuroguard.assuranceservice.dto.AssuranceResponseDto;
import com.neuroguard.assuranceservice.dto.UserDto;
import com.neuroguard.assuranceservice.service.AssuranceService;
import com.neuroguard.assuranceservice.service.PDFGenerationService;
import com.neuroguard.assuranceservice.service.UserServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/assurances")
@Slf4j
public class AssuranceReportController {

    @Autowired
    private AssuranceService assuranceService;

    @Autowired
    private PDFGenerationService pdfGenerationService;

    @Autowired
    private UserServiceClient userServiceClient;

    /**
     * Download individual assurance PDF report
     * GET /api/assurances/{id}/report/pdf
     */
    @GetMapping("/{id}/report/pdf")
    public ResponseEntity<byte[]> downloadAssurancePDF(@PathVariable Long id) {
        try {
            log.info("Downloading PDF for assurance ID: {}", id);

            // Fetch assurance
            AssuranceResponseDto assurance = assuranceService.getAssuranceById(id);
            if (assurance == null) {
                log.warn("Assurance not found: {}", id);
                return ResponseEntity.notFound().build();
            }

            // Fetch user details
            UserDto user = null;
            try {
                user = userServiceClient.getUserById(assurance.getPatientId());
            } catch (Exception e) {
                log.warn("Could not fetch user details for patient {}: {}", assurance.getPatientId(), e.getMessage());
            }

            // Generate PDF
            byte[] pdfContent = pdfGenerationService.generateAssurancePDF(assurance, user);

            // Return with proper headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "assurance_" + id + ".pdf");

            log.info("✓ PDF downloaded successfully for assurance ID: {}", id);
            return ResponseEntity.ok().headers(headers).body(pdfContent);

        } catch (Exception e) {
            log.error("Error downloading PDF for assurance {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Bulk export multiple assurances as PDF
     * POST /api/assurances/reports/bulk-export
     */
    @PostMapping("/reports/bulk-export")
    public ResponseEntity<byte[]> bulkExportAssurancePDF(@RequestBody List<Long> assuranceIds) {
        try {
            log.info("Bulk exporting {} assurance(s) to PDF", assuranceIds.size());

            if (assuranceIds == null || assuranceIds.isEmpty()) {
                log.warn("No assurance IDs provided for bulk export");
                return ResponseEntity.badRequest().build();
            }

            // Fetch all assurances
            List<AssuranceResponseDto> assurances = assuranceService.getAssurancesByIds(assuranceIds);
            if (assurances.isEmpty()) {
                log.warn("No assurances found for the provided IDs");
                return ResponseEntity.notFound().build();
            }

            // Fetch user details (use first patient as reference)
            UserDto user = null;
            if (!assurances.isEmpty()) {
                try {
                    user = userServiceClient.getUserById(assurances.get(0).getPatientId());
                } catch (Exception e) {
                    log.warn("Could not fetch user details: {}", e.getMessage());
                }
            }

            // Generate bulk PDF
            byte[] pdfContent = pdfGenerationService.generateBulkAssurancePDF(assurances, user);

            // Return with proper headers
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss"));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "assurances_export_" + timestamp + ".pdf");

            log.info("✓ Bulk PDF exported successfully - {} records", assurances.size());
            return ResponseEntity.ok().headers(headers).body(pdfContent);

        } catch (Exception e) {
            log.error("Error bulk exporting assurances to PDF: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}
