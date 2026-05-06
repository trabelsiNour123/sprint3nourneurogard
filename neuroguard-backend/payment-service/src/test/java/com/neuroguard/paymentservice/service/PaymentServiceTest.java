package com.neuroguard.paymentservice.service;

import com.neuroguard.paymentservice.client.ProductOrderClient;
import com.neuroguard.paymentservice.dto.CreatePaymentRequest;
import com.neuroguard.paymentservice.dto.OrderSummaryDto;
import com.neuroguard.paymentservice.dto.PaymentResponse;
import com.neuroguard.paymentservice.entity.Payment;
import com.neuroguard.paymentservice.entity.PaymentStatus;
import com.neuroguard.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    private PaymentRepository paymentRepository;
    private ProductOrderClient productOrderClient;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        productOrderClient = mock(ProductOrderClient.class);
        paymentService = new PaymentService(paymentRepository, productOrderClient);
    }

    @Test
    void createPayment_success() {
        CreatePaymentRequest req = new CreatePaymentRequest();
        req.setOrderId(10L);
        req.setAmount(new BigDecimal("100.00"));
        req.setPaymentMethod(" card ");

        OrderSummaryDto order = new OrderSummaryDto();
        order.setId(10L);
        order.setStatus("ACTIVE");
        order.setTotal(new BigDecimal("100.00"));

        when(productOrderClient.getOrderById(10L)).thenReturn(order);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        Payment saved = Payment.builder()
                .id(1L)
                .orderId(10L)
                .amount(req.getAmount())
                .paymentMethod("card")
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentRepository.save(captor.capture())).thenReturn(saved);

        PaymentResponse resp = paymentService.createPayment(req);

        assertNotNull(resp);
        assertEquals(1L, resp.getId());
        assertEquals(10L, resp.getOrderId());
        assertEquals(new BigDecimal("100.00"), resp.getAmount());
        assertEquals(PaymentStatus.PENDING, resp.getStatus());

        Payment captured = captor.getValue();
        assertEquals("card", captured.getPaymentMethod());
    }

    @Test
    void updatePaymentStatus_invalidTransition_throws() {
        // prepare existing payment with REFUNDED status
        Payment existing = Payment.builder()
                .id(2L)
                .status(PaymentStatus.REFUNDED)
                .build();

        when(paymentRepository.findById(2L)).thenReturn(Optional.of(existing));

        var req = new com.neuroguard.paymentservice.dto.UpdatePaymentStatusRequest();
        req.setStatus(PaymentStatus.PENDING);

        assertThrows(com.neuroguard.paymentservice.exception.BusinessException.class,
                () -> paymentService.updatePaymentStatus(2L, req));
    }
}
