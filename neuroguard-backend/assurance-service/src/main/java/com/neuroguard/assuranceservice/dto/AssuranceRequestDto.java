package com.neuroguard.assuranceservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AssuranceRequestDto {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotBlank(message = "Provider Name is required")
    @Size(min = 3, max = 100, message = "Provider Name must be between 3 and 100 characters")
    private String providerName;

    @NotBlank(message = "Policy Number is required")
    @Size(min = 3, max = 50, message = "Policy Number must be between 3 and 50 characters")
    private String policyNumber;

    @Size(max = 500, message = "Coverage Details cannot exceed 500 characters")
    private String coverageDetails;

    @NotBlank(message = "Illness is required")
    @Size(min = 3, max = 200, message = "Illness must be between 3 and 200 characters")
    private String illness;

    @NotBlank(message = "Postal Code is required")
    @Pattern(regexp = "^[0-9]{5}$", message = "Postal Code must be exactly 5 digits")
    private String postalCode;

    @NotBlank(message = "Mobile Phone is required")
    @Pattern(regexp = "^[\\+]?[(]?[0-9]{3}[)]?[-\\s\\.]?[0-9]{3}[-\\s\\.]?[0-9]{4,6}$",
             message = "Mobile Phone must be a valid phone number format")
    private String mobilePhone;
}
