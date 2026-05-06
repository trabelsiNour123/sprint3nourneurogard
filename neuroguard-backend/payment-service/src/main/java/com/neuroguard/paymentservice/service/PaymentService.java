package com.neuroguard.paymentservice.service;

import com.neuroguard.paymentservice.client.ProductOrderClient;
import com.neuroguard.paymentservice.dto.CreatePaymentRequest;
import com.neuroguard.paymentservice.dto.OrderSummaryDto;
import com.neuroguard.paymentservice.dto.PaymentResponse;
import com.neuroguard.paymentservice.dto.UpdatePaymentStatusRequest;
import com.neuroguard.paymentservice.entity.Payment;
import com.neuroguard.paymentservice.entity.PaymentStatus;
import com.neuroguard.paymentservice.exception.BusinessException;
import com.neuroguard.paymentservice.exception.ResourceNotFoundException;
import com.neuroguard.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ProductOrderClient productOrderClient;

    public PaymentResponse createPayment(CreatePaymentRequest request) {
        OrderSummaryDto order = productOrderClient.getOrderById(request.getOrderId());
        validateOrderForPayment(order, request);

        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod().trim())
                .status(PaymentStatus.PENDING)
                .build();

        return mapToResponse(paymentRepository.save(payment));
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        return mapToResponse(findPayment(id));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findByOrderIdOrderByCreatedAtDesc(orderId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public PaymentResponse updatePaymentStatus(Long id, UpdatePaymentStatusRequest request) {
        Payment payment = findPayment(id);
        validateStatusTransition(payment.getStatus(), request.getStatus());
        payment.setStatus(request.getStatus());
        return mapToResponse(paymentRepository.save(payment));
    }

    private Payment findPayment(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + id));
    }

    private void validateOrderForPayment(OrderSummaryDto order, CreatePaymentRequest request) {
        if (order == null || order.getId() == null) {
            throw new ResourceNotFoundException("Order not found with ID: " + request.getOrderId());
        }
        if ("CANCELLED".equalsIgnoreCase(order.getStatus())) {
            throw new BusinessException("Cannot create payment for a cancelled order");
        }
        if (order.getTotal() != null && request.getAmount().compareTo(order.getTotal()) < 0) {
            throw new BusinessException("Payment amount cannot be lower than the order total");
        }
    }

    private void validateStatusTransition(PaymentStatus currentStatus, PaymentStatus newStatus) {
        if (currentStatus == PaymentStatus.REFUNDED) {
            throw new BusinessException("Refunded payments cannot be updated");
        }
        if (currentStatus == PaymentStatus.FAILED && newStatus == PaymentStatus.REFUNDED) {
            throw new BusinessException("A failed payment cannot be refunded");
        }
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
