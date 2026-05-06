package com.neuroguard.paymentservice.client;

import com.neuroguard.paymentservice.dto.OrderSummaryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "product-order-service",
        url = "${services.product-order.url:http://localhost:8095}"
)
public interface ProductOrderClient {

    @GetMapping("/orders/internal/{id}")
    OrderSummaryDto getOrderById(@PathVariable("id") Long id);
}
