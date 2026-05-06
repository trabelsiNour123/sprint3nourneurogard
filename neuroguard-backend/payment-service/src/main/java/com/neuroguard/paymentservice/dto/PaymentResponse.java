package com.neuroguard.paymentservice.dto;

import com.neuroguard.paymentservice.entity.PaymentStatus;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder
public class PaymentResponse {
    Long id;
    Long orderId;
    BigDecimal amount;
    PaymentStatus status;
    String paymentMethod;
    LocalDateTime createdAt;
}
