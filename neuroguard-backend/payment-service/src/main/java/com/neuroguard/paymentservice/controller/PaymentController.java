package com.neuroguard.paymentservice.controller;

import com.neuroguard.paymentservice.dto.CreatePaymentRequest;
import com.neuroguard.paymentservice.dto.PaymentResponse;
import com.neuroguard.paymentservice.dto.UpdatePaymentStatusRequest;
import com.neuroguard.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        return paymentService.createPayment(request);
    }

    @GetMapping("/{id}")
    public PaymentResponse getPaymentById(@PathVariable Long id) {
        return paymentService.getPaymentById(id);
    }

    @GetMapping
    public List<PaymentResponse> getPayments(@RequestParam(required = false) Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId query parameter is required");
        }
        return paymentService.getPaymentsByOrderId(orderId);
    }

    @PutMapping("/{id}/status")
    public PaymentResponse updatePaymentStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePaymentStatusRequest request) {
        return paymentService.updatePaymentStatus(id, request);
    }
}
