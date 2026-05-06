package com.neuroguard.productorderservice.service;

import com.neuroguard.productorderservice.dto.DeliveryCreateRequest;
import com.neuroguard.productorderservice.dto.DeliveryUpdateRequest;
import com.neuroguard.productorderservice.entity.Delivery;
import com.neuroguard.productorderservice.entity.Order;
import com.neuroguard.productorderservice.entity.PromoCode;
import com.neuroguard.productorderservice.exception.ResourceNotFoundException;
import com.neuroguard.productorderservice.repository.DeliveryRepository;
import com.neuroguard.productorderservice.repository.OrderRepository;
import com.neuroguard.productorderservice.repository.PromoCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final PromoCodeRepository promoCodeRepository;
    public Delivery create(Long orderId, DeliveryCreateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        if (deliveryRepository.existsByOrder_Id(orderId)) {
            throw new IllegalStateException("A delivery already exists for this order");
        }
        Delivery delivery = new Delivery();
        delivery.setOrder(order);
        delivery.setAddress(request.getAddress().trim());
        delivery.setStatus(request.getStatus());
//        delivery.setDeliveryDate(
//                request.getDeliveryDate() != null ? request.getDeliveryDate() : LocalDateTime.now());

        // ==========================================
        // 1. SMART SCHEDULING DELIVERY TIME
        // ==========================================
        scheduleDelivery(order,delivery);

        // ==========================================
        // 2. DYNAMIC COST CALCULATION
        // ==========================================
        calculateDeliveryCost(delivery,order);

        // ==========================================
        // 3. PROMO CODE APPLICATION
        // ==========================================
        if (order.getAppliedPromoCode() != null && !order.getAppliedPromoCode().isBlank()) {
            applyLogisticsPromo(delivery, order);
        }
        order.setDelivery(delivery);
        return deliveryRepository.save(delivery);
    }

    @Transactional(readOnly = true)
    public Page<Delivery> findPage(String search, Pageable pageable) {
        if (search == null || search.isBlank()) {
            return deliveryRepository.findAll(pageable);
        }
        return deliveryRepository.searchDeliveries(search.trim(), pageable);
    }

    @Transactional(readOnly = true)
    public Delivery getById(Long id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found: " + id));
    }

    public Delivery update(Long id, DeliveryUpdateRequest request) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found: " + id));
        delivery.setDeliveryDate(request.getDeliveryDate());
        delivery.setAddress(request.getAddress().trim());
        delivery.setStatus(request.getStatus());
        return deliveryRepository.save(delivery);
    }

    public void delete(Long id) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found: " + id));
        Order order = delivery.getOrder();
        if (order != null) {
            order.setDelivery(null);
        }
        deliveryRepository.delete(delivery);
    }
    private void calculateDeliveryCost(Delivery delivery, Order order) {
        int totalItems = order.getLines().stream()
                .mapToInt(line -> line.getQuantity())
                .sum();
        double totalValue = order.getTotal();

        double baseCost = 5.0;
        double quantityCost = totalItems * 0.5; // Weight proxy

        double valueCost = 0.0;
        if (totalValue < 50) valueCost = 2.0;
        if (totalValue > 500) valueCost = -3.0;     // discount for big orders


        double totalCost = baseCost + quantityCost + valueCost;

        // Prevent negative fees just in case valueCost creates a negative total
        delivery.setFee(Math.max(0.0, totalCost));
    }
    private void applyLogisticsPromo(Delivery delivery, Order order) {
        String codeString = order.getAppliedPromoCode().trim();

        // 1. Check if PromoCode exists in DB
        Optional<PromoCode> promoOpt = promoCodeRepository.findByCodeIgnoreCase(codeString);
        if (promoOpt.isEmpty()) {
            // Note: OrderService should have caught this during checkout.
            // If it reaches here, the code is invalid, so we just return without discounting.
            return;
        }

        PromoCode promo = promoOpt.get();

        // 2. Check if active
        if (!promo.isActive()) {
            return;
        }

        // 3. Time Validity Check (Compare against ORDER creation date, not current date)
        LocalDateTime orderDate = order.getOrderDate();
        if (promo.getValidFrom() != null && orderDate.isBefore(promo.getValidFrom())) {
            return; // Order was placed before promo started
        }
        if (promo.getValidUntil() != null && orderDate.isAfter(promo.getValidUntil())) {
            return; // Order was placed after promo expired
        }

        // 4. Evaluate: Free Delivery Threshold
        if (promo.getFreeDeliveryThreshold() != null && order.getTotal() >= promo.getFreeDeliveryThreshold()) {
            delivery.setFee(0.0);
            delivery.setAddress(delivery.getAddress() + " [PROMO: THRESHOLD MET - FREE DELIVERY]");
            return; // Exit early, no need to apply percentage if it's already free
        }

        // 5. Evaluate: Percentage Discount on Shipping Fee
        if (promo.getDiscountPercent() != null && promo.getDiscountPercent() > 0.0) {
            double currentFee = delivery.getFee();
            double discountAmount = currentFee * (promo.getDiscountPercent() / 100.0);
            double newFee = Math.max(0.0, currentFee - discountAmount);

            delivery.setFee(newFee);
            delivery.setAddress(delivery.getAddress() + String.format(" [PROMO: %.0f%% OFF SHIPPING]", promo.getDiscountPercent()));
        }
    }

    private void scheduleDelivery(Order order, Delivery delivery) {

        int totalItems = order.getLines().stream()
                .mapToInt(oline -> oline.getQuantity())
                .sum();

        double totalValue = order.getTotal();

        int deliveryDays;

        // Smart rules
        if (totalValue > 1000) {
            deliveryDays = 1; // priority delivery
        } else if (totalItems > 10) {
            deliveryDays = 5; // large order
        } else {
            deliveryDays = 2; // normal
        }

        delivery.setDeliveryDate(LocalDateTime.now().plusDays(deliveryDays));


    }
}
