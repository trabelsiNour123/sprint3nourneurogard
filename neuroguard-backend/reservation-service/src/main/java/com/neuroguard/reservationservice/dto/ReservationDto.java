package com.neuroguard.reservationservice.dto;

import com.neuroguard.reservationservice.entity.ConsultationType;
import com.neuroguard.reservationservice.entity.ReservationStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class ReservationDto {
    private Long id;
    private Long patientId;
    private Long providerId;
    private LocalDateTime reservationDate;
    private LocalTime timeSlot;
    private ConsultationType consultationType;
    private ReservationStatus status;
    private String notes;
    private Long consultationId;
    private LocalDateTime createdAt;
    
    // Optional fields for enrichment (frontend display)
    private String patientName;
    private String providerName;
}
