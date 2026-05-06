package com.neuroguard.assuranceservice.dto;

import com.neuroguard.assuranceservice.entity.AssuranceStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AssuranceResponseDto {
    private Long id;
    private Long patientId;
    private UserDto patientDetails; // Pulled from user-service via Feign
    private String providerName;
    private String policyNumber;
    private String coverageDetails;
    private String illness;
    private String postalCode;
    private String mobilePhone;
    private AssuranceStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
