package com.neuroguard.productorderservice;

import com.neuroguard.productorderservice.dto.DeliveryCreateRequest;
import com.neuroguard.productorderservice.entity.*;
import com.neuroguard.productorderservice.repository.DeliveryRepository;
import com.neuroguard.productorderservice.repository.OrderRepository;
import com.neuroguard.productorderservice.repository.PromoCodeRepository;
import com.neuroguard.productorderservice.service.DeliveryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DeliveryServiceMockTest {
    @Mock
    private DeliveryRepository deliveryRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private PromoCodeRepository promoCodeRepository;

    @InjectMocks
    private DeliveryService deliveryService;
    private Order sampleOrder;
    @BeforeEach
    void setUp() {
        // Create a basic order
        Product product = new Product();
        product.setId(1L);
        product.setPrice(100.0);

        OrderLine line = new OrderLine();
        line.setProduct(product);
        line.setQuantity(2);
        sampleOrder = new Order();
        sampleOrder.setId(1L);
        sampleOrder.setTotal(200.0);
        sampleOrder.setOrderDate(LocalDateTime.now());
        sampleOrder.setLines(List.of(line));
    }
    @Test
    void create_WithHighValueOrder_ShouldSchedulePriorityDelivery() {
        // Arrange
        DeliveryCreateRequest request = new DeliveryCreateRequest();
        request.setAddress("Tunis");
        request.setStatus(DeliveryStatus.PENDING);
        sampleOrder.setTotal(1500.0);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Delivery result = deliveryService.create(1L, request);

        // Assert
        assertEquals(1, result.getDeliveryDate().getDayOfYear() - LocalDateTime.now().getDayOfYear());

        assertEquals(3.0, result.getFee());
    }
   // TEST: PromoCode Free Delivery
    @Test
    void shouldApplyFreeDeliveryPromo() {
        DeliveryCreateRequest request = new DeliveryCreateRequest();
        request.setAddress("Tunis");
        request.setStatus(DeliveryStatus.PENDING);

        sampleOrder.setAppliedPromoCode("FREEDEL");

        PromoCode promo = new PromoCode();
        promo.setCode("FREEDEL");
        promo.setActive(true);
        promo.setFreeDeliveryThreshold(0.0);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(deliveryRepository.existsByOrder_Id(1L)).thenReturn(false);
        when(promoCodeRepository.findByCodeIgnoreCase("FREEDEL"))
                .thenReturn(Optional.of(promo));
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(i -> i.getArguments()[0]);
        Delivery result = deliveryService.create(1L, request);

        assertEquals(0.0, result.getFee());
    }
    @Test
    void shouldIgnoreExpiredPromoCode() {
        DeliveryCreateRequest request = new DeliveryCreateRequest();
        request.setAddress("Tunis");
        request.setStatus(DeliveryStatus.PENDING);

        sampleOrder.setAppliedPromoCode("OLDPROMO");

        PromoCode promo = new PromoCode();
        promo.setCode("OLDPROMO");
        promo.setActive(true);
        promo.setValidUntil(LocalDateTime.now().minusDays(1)); // expired

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(deliveryRepository.existsByOrder_Id(1L)).thenReturn(false);
        when(promoCodeRepository.findByCodeIgnoreCase("OLDPROMO"))
                .thenReturn(Optional.of(promo));
        when(deliveryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Delivery result = deliveryService.create(1L, request);

        assertTrue(result.getFee() > 0); // no discount applied
    }
    // TEST : Delivery already exists
    @Test
    void shouldThrowWhenDeliveryAlreadyExists() {
        DeliveryCreateRequest request = new DeliveryCreateRequest();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(deliveryRepository.existsByOrder_Id(1L)).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> deliveryService.create(1L, request));
    }
}
