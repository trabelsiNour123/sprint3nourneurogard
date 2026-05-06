package com.neuroguard.paymentservice.dto;

import com.neuroguard.paymentservice.entity.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePaymentStatusRequest {

    @NotNull(message = "Payment status is required")
    private PaymentStatus status;
}
