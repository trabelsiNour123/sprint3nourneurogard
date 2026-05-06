package com.neuroguard.monitoringservice.service;

import com.neuroguard.monitoringservice.entity.VitalsEntity;
import com.neuroguard.monitoringservice.repository.VitalsRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportGenerationService {

    private final VitalsRepository vitalsRepository;
    private final JavaMailSender mailSender;

    public ReportGenerationService(VitalsRepository vitalsRepository, JavaMailSender mailSender) {
        this.vitalsRepository = vitalsRepository;
        this.mailSender = mailSender;
    }

    // Cron job for Every Sunday at midnight
    @Scheduled(cron = "0 0 0 * * SUN")
    public void evaluateAndGenerateReports() {
        // In a real scenario, fetch all patients from active DB
        String dummyPatientId = "2";
        System.out.println("Evaluating report generation for Patient: " + dummyPatientId);
        
        List<VitalsEntity> allVitals = vitalsRepository.findByPatientId(dummyPatientId);
        if (allVitals.isEmpty()) return;

        boolean isSevere = calculateSeverity(allVitals);
        System.out.println("7-Day Severity Check: " + (isSevere ? "SEVERE" : "STABLE"));

        // Fetch cross-service user data
        String patientName = "Patient " + dummyPatientId;
        String doctorEmail = "doctor@neuroguard-health.com";
        try {
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> userData = restTemplate.getForObject("http://localhost:8081/users/" + dummyPatientId, Map.class);
            if (userData != null) {
                if (userData.containsKey("name")) patientName = (String) userData.get("name");
                if (userData.containsKey("doctorEmail") && userData.get("doctorEmail") != null) {
                    doctorEmail = (String) userData.get("doctorEmail");
                }
            }
        } catch (Exception e) {
            System.err.println("Could not hit user-service to fetch patient name/doctor: " + e.getMessage());
        }
        
        if (isSevere) {
            System.out.println("Severity Threshold Met. Generating Weekly Escalation Report...");
            byte[] excelFile = generateExcelReport(patientName, allVitals, "Weekly Escalation Report");
            sendReportEmail(doctorEmail, patientName, excelFile, true);
        } else {
            
            LocalDate today = LocalDate.now();
            if (today.getDayOfMonth() == today.lengthOfMonth()) {
                System.out.println("Generating Standard Monthly Report...");
                byte[] excelFile = generateExcelReport(patientName, allVitals, "Monthly Wellness Report");
                sendReportEmail(doctorEmail, patientName, excelFile, false);
            }
        }
    }

    // A manual trigger method for the endpoint
    public void generateManualReport(String patientId) {
        String patientName = "Patient " + patientId;
        String doctorEmail = "doctor@neuroguard-health.com";
        
        try {
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> userData = restTemplate.getForObject("http://localhost:8081/users/" + patientId, Map.class);
            if (userData != null) {
                if (userData.containsKey("name")) patientName = (String) userData.get("name");
                if (userData.containsKey("doctorEmail") && userData.get("doctorEmail") != null) {
                    doctorEmail = (String) userData.get("doctorEmail");
                }
            }
        } catch (Exception e) {
            System.err.println("Could not hit user-service to fetch patient name/doctor: " + e.getMessage());
        }

        List<VitalsEntity> allVitals = vitalsRepository.findByPatientId(patientId);
        boolean isSevere = calculateSeverity(allVitals);
        
        String reportType = isSevere ? "Manual Escalation Report" : "Manual Doctor Request Report";
        byte[] excelFile = generateExcelReport(patientName, allVitals, reportType);
        sendReportEmail(doctorEmail, patientName, excelFile, isSevere);
    }

    private boolean calculateSeverity(List<VitalsEntity> allVitals) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        long abnormalSpikes = allVitals.stream()
            .filter(v -> v.getTimestamp().isAfter(oneWeekAgo))
            .filter(v -> "warning".equals(v.getStatus()) || v.getHeartRate() > 100)
            .count();
        return abnormalSpikes > 3;
    }

    private byte[] generateExcelReport(String patientName, List<VitalsEntity> vitals, String reportType) {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Sheet 1: Exec Summary
            Sheet summarySheet = workbook.createSheet("Executive Summary");
            Row row0 = summarySheet.createRow(0);
            row0.createCell(0).setCellValue("NeuroGuard Patient Report");
            summarySheet.createRow(1).createCell(0).setCellValue("Report Type: " + reportType);
            summarySheet.createRow(2).createCell(0).setCellValue("Patient: " + patientName);
            summarySheet.createRow(3).createCell(0).setCellValue("Date Generated: " + LocalDate.now().toString());

            // Sheet 2: Vitals Heatmap
            Sheet vitalsSheet = workbook.createSheet("Vitals Heatmap");
            Row headerRow = vitalsSheet.createRow(0);
            String[] columns = {"Date", "Avg HR", "Max HR", "Min HR", "Avg BP (Sys)", "Avg SpO2", "Warnings"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            CellStyle warningStyle = workbook.createCellStyle();
            warningStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
            warningStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Map<LocalDate, List<VitalsEntity>> byDate = vitals.stream()
                .collect(Collectors.groupingBy(v -> v.getTimestamp().toLocalDate()));
            
            int rowIndex = 1;
            List<LocalDate> sortedDates = new ArrayList<>(byDate.keySet());
            Collections.sort(sortedDates);

            for (LocalDate date : sortedDates) {
                List<VitalsEntity> dayVitals = byDate.get(date);
                double avgHr = dayVitals.stream().mapToInt(VitalsEntity::getHeartRate).average().orElse(0);
                int maxHr = dayVitals.stream().mapToInt(VitalsEntity::getHeartRate).max().orElse(0);
                int minHr = dayVitals.stream().mapToInt(VitalsEntity::getHeartRate).min().orElse(0);
                double avgSys = dayVitals.stream().mapToInt(VitalsEntity::getSystolicBloodPressure).average().orElse(0);
                double avgSpo2 = dayVitals.stream().mapToInt(VitalsEntity::getOxygenSaturation).average().orElse(0);
                long warnings = dayVitals.stream().filter(v -> "warning".equals(v.getStatus())).count();

                Row row = vitalsSheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(date.toString());
                row.createCell(1).setCellValue(Math.round(avgHr));
                
                Cell maxHrCell = row.createCell(2);
                maxHrCell.setCellValue(maxHr);
                if (maxHr > 100) {
                    maxHrCell.setCellStyle(warningStyle);
                }
                
                row.createCell(3).setCellValue(minHr);
                row.createCell(4).setCellValue(Math.round(avgSys));
                
                Cell spo2Cell = row.createCell(5);
                spo2Cell.setCellValue(Math.round(avgSpo2));
                if (avgSpo2 < 95) {
                    spo2Cell.setCellStyle(warningStyle);
                }

                Cell warningsCell = row.createCell(6);
                warningsCell.setCellValue(warnings);
                if (warnings > 0) {
                    warningsCell.setCellStyle(warningStyle);
                }
            }
            
            for (int i = 0; i < columns.length; i++) {
                vitalsSheet.autoSizeColumn(i);
            }

            Sheet behavioralSheet = workbook.createSheet("Behavioral & Cognitive");
            behavioralSheet.createRow(0).createCell(0).setCellValue("Daily Memory Match & Word Recall outcomes and Mood (Data sourced from Wellbeing Service)");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void sendReportEmail(String toEmail, String patientName, byte[] excelData, boolean isUrgent) {
        if (excelData == null) return;
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // true = multipart (allows attachments)
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(toEmail);
            
            String subject = isUrgent ? "URGENT: Weekly NeuroGuard Escalation Report - " + patientName 
                                      : "Monthly NeuroGuard Wellness Report - " + patientName;
            helper.setSubject(subject);
            
            String htmlBody = "<div style='font-family: Arial, sans-serif; max-width: 600px; padding: 20px; border: 1px solid #e2e8f0; border-radius: 8px;'>"
                            + "<h2 style='color: #1e293b; border-bottom: 2px solid " + (isUrgent ? "#ef4444" : "#4099ff") + "; padding-bottom: 10px;'>" 
                            + (isUrgent ? "Action Required: Escalation Report" : "Monthly Status Report") + "</h2>"
                            + "<p style='color: #475569; font-size: 16px;'>Dear Doctor,</p>"
                            + "<p style='color: #475569; font-size: 16px;'>Attached is the requested NeuroGuard wellness report for <b>" + patientName + "</b>.</p>"
                            + "<div style='background: " + (isUrgent ? "#fee2e2" : "#f1f5f9") + "; padding: 15px; border-radius: 8px; margin: 20px 0;'>"
                            + (isUrgent ? "<b style='color: #dc2626;'>Alert:</b> Multiple irregular vitals detected in the past 7 days. Please review the 'Vitals Heatmap' tab." 
                                        : "Routine synthesis of daily vitals and wellbeing indicators is stable.")
                            + "</div>"
                            + "<p style='color: #475569; font-size: 14px;'><i>Confidential Patient Information. Do not forward.</i></p>"
                            + "</div>";

            helper.setText(htmlBody, true); // true = isHtml
            
            String safeName = patientName.replaceAll("[^a-zA-Z0-9]", "_");
            String filename = "NeuroGuard_Report_" + safeName + "_" + LocalDate.now() + ".xlsx";
            helper.addAttachment(filename, new ByteArrayResource(excelData));

            mailSender.send(message);
            System.out.println("Successfully sent professional email to " + toEmail);

        } catch (org.springframework.mail.MailAuthenticationException e) {
            System.err.println("=================================================");
            System.err.println("EMAIL DELIVERY FAILED: Authentication Error.");
            System.err.println("Google rejected your email credentials. Make sure there are no spaces in your App Password inside application.properties.");
            System.err.println("Excel file WAS generated successfully, but the physical email to " + toEmail + " was blocked by Gmail.");
            System.err.println("=================================================");
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
}
