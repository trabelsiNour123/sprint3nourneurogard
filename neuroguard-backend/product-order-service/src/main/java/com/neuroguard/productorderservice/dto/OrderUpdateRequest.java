package com.neuroguard.productorderservice.dto;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data
public class OrderUpdateRequest {

    /** When set, replaces order lines and adjusts stock accordingly. */
    @Valid
    private List<OrderLineItemRequest> lines;

    private String status;
}
