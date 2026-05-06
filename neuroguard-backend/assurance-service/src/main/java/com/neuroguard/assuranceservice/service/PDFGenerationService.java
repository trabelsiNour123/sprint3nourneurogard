package com.neuroguard.assuranceservice.service;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.neuroguard.assuranceservice.dto.AssuranceResponseDto;
import com.neuroguard.assuranceservice.dto.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PDFGenerationService {

    @Autowired(required = false)
    private TemplateEngine templateEngine;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Generate PDF for a single assurance record
     */
    public byte[] generateAssurancePDF(AssuranceResponseDto assurance, UserDto user) {
        try {
            log.info("Generating PDF for assurance ID: {}", assurance.getId());

            // Create HTML content from template
            String htmlContent = renderTemplate(assurance, user);

            // Convert HTML to PDF
            return convertHtmlToPdf(htmlContent);

        } catch (Exception e) {
            log.error("Error generating PDF for assurance {}: {}", assurance.getId(), e.getMessage(), e);
            throw new RuntimeException("PDF generation failed for assurance " + assurance.getId(), e);
        }
    }

    /**
     * Generate PDF for multiple assurance records (bulk export)
     */
    public byte[] generateBulkAssurancePDF(List<AssuranceResponseDto> assurances, UserDto user) {
        try {
            log.info("Generating bulk PDF export for {} assurance(s)", assurances.size());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            // Add title
            Paragraph title = new Paragraph("NeuroGuard Insurance Report - Bulk Export");
            title.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(title);

            // Add timestamp
            Paragraph timestamp = new Paragraph("Generated: " + LocalDateTime.now().format(formatter));
            timestamp.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(timestamp);

            // Add each assurance
            for (int i = 0; i < assurances.size(); i++) {
                if (i > 0) {
                    document.newPage(); // New page for each assurance
                }

                String htmlContent = renderTemplate(assurances.get(i), user);
                // For simplicity, just add the HTML as text
                document.add(new Paragraph(htmlContent));
            }

            document.close();

            log.info("✓ Bulk PDF generated successfully - {} pages", assurances.size());
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating bulk PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Bulk PDF generation failed", e);
        }
    }

    /**
     * Render HTML template for PDF
     */
    private String renderTemplate(AssuranceResponseDto assurance, UserDto user) {
        // Simple HTML generation if template engine not available
        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family: Arial, sans-serif; margin: 40px;'>");
        html.append("<div style='border-bottom: 3px solid #667eea; padding-bottom: 20px; margin-bottom: 30px;'>");
        html.append("<h1 style='color: #667eea; margin: 0;'>Insurance Policy Report</h1>");
        html.append("<p style='color: #999; margin: 10px 0 0 0;'>Generated: ").append(LocalDateTime.now().format(formatter)).append("</p>");
        html.append("</div>");

        // Patient Information
        html.append("<div style='margin: 20px 0;'>");
        html.append("<h2 style='color: #2c3e50; font-size: 14px; margin-bottom: 10px;'>PATIENT INFORMATION</h2>");
        if (user != null) {
            html.append("<p style='margin: 5px 0;'><strong>Name:</strong> ").append(user.getFirstName()).append(" ").append(user.getLastName()).append("</p>");
            html.append("<p style='margin: 5px 0;'><strong>Email:</strong> ").append(user.getEmail()).append("</p>");
        }
        html.append("</div>");

        // Insurance Details
        html.append("<div style='margin: 20px 0;'>");
        html.append("<h2 style='color: #2c3e50; font-size: 14px; margin-bottom: 10px;'>INSURANCE DETAILS</h2>");
        html.append("<p style='margin: 5px 0;'><strong>Provider:</strong> ").append(assurance.getProviderName()).append("</p>");
        html.append("<p style='margin: 5px 0;'><strong>Policy Number:</strong> <code style='background: #f0f4f8; padding: 2px 6px; color: #0ea5e9;'>")
                .append(assurance.getPolicyNumber()).append("</code></p>");
        html.append("<p style='margin: 5px 0;'><strong>Illness/Condition:</strong> ").append(assurance.getIllness()).append("</p>");
        html.append("<p style='margin: 5px 0;'><strong>Postal Code:</strong> ").append(assurance.getPostalCode()).append("</p>");
        html.append("<p style='margin: 5px 0;'><strong>Mobile Phone:</strong> ").append(assurance.getMobilePhone()).append("</p>");
        html.append("</div>");

        // Status
        html.append("<div style='margin: 20px 0;'>");
        html.append("<h2 style='color: #2c3e50; font-size: 14px; margin-bottom: 10px;'>STATUS</h2>");
        String statusColor = getStatusColor(assurance.getStatus().toString());
        html.append("<span style='background: ").append(statusColor).append("; color: white; padding: 5px 10px; border-radius: 20px; font-weight: bold;'>")
                .append(assurance.getStatus().toString()).append("</span>");
        html.append("</div>");

        // Dates
        html.append("<div style='margin: 20px 0; color: #999; font-size: 12px;'>");
        html.append("<p>Created: ").append(assurance.getCreatedAt().format(formatter)).append("</p>");
        if (assurance.getUpdatedAt() != null) {
            html.append("<p>Updated: ").append(assurance.getUpdatedAt().format(formatter)).append("</p>");
        }
        html.append("</div>");

        html.append("</body></html>");

        return html.toString();
    }

    /**
     * Get color for status badge
     */
    private String getStatusColor(String status) {
        switch (status) {
            case "APPROVED":
                return "#10b981";
            case "REJECTED":
                return "#ef4444";
            case "PENDING":
                return "#f59e0b";
            default:
                return "#667eea";
        }
    }

    /**
     * Convert HTML to PDF (simple implementation)
     */
    private byte[] convertHtmlToPdf(String htmlContent) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, baos);
        document.open();

        // Simple text conversion - for production, use a proper HTML-to-PDF library
        Paragraph para = new Paragraph(htmlContent.replaceAll("<[^>]*>", ""));
        document.add(para);

        document.close();
        return baos.toByteArray();
    }
}
