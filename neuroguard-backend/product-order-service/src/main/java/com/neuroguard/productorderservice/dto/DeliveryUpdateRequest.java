package com.neuroguard.productorderservice.dto;

import com.neuroguard.productorderservice.entity.DeliveryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeliveryUpdateRequest {

    @NotNull(message = "Delivery date is required")
    private LocalDateTime deliveryDate;

    @NotBlank(message = "Address is required")
    @Size(max = 500, message = "Address must be at most 500 characters")
    private String address;

    @NotNull(message = "Delivery status is required")
    private DeliveryStatus status;
}
