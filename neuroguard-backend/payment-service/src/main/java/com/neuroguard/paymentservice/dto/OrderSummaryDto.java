package com.neuroguard.paymentservice.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderSummaryDto {
    private Long id;
    private BigDecimal total;
    private String status;
}
