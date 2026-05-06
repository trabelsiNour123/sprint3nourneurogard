package com.neuroguard.productorderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateRequest {

    private Long userId;
    private String status;
    private String appliedPromoCode;

    @NotEmpty
    @Valid
    private List<OrderLineItemRequest> lines;
}
