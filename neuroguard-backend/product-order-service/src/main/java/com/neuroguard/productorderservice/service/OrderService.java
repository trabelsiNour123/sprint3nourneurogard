package com.neuroguard.productorderservice.service;

import com.neuroguard.productorderservice.dto.OrderCreateRequest;
import com.neuroguard.productorderservice.dto.OrderLineItemRequest;
import com.neuroguard.productorderservice.dto.OrderUpdateRequest;
import com.neuroguard.productorderservice.entity.Order;
import com.neuroguard.productorderservice.entity.OrderLine;
import com.neuroguard.productorderservice.entity.Product;
import com.neuroguard.productorderservice.client.UserClient;
import com.neuroguard.productorderservice.dto.UserDto;
import com.neuroguard.productorderservice.exception.ResourceNotFoundException;
import com.neuroguard.productorderservice.repository.OrderRepository;
import com.neuroguard.productorderservice.repository.ProductRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserClient userClient;

    public Order create(OrderCreateRequest request) {
        if (request.getUserId() != null) {
            try {
                // Feign calls user-service to check if user exists!
                UserDto user = userClient.getUserById(request.getUserId());
            } catch (FeignException.NotFound e) {
                throw new ResourceNotFoundException("User not found with ID: " + request.getUserId());
            } catch (FeignException e) {
                throw new IllegalStateException("Error communicating with user-service", e);
            }
        }

        Order order = new Order();
        order.setUserId(request.getUserId()); // Save user association
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(
                request.getStatus() != null && !request.getStatus().isBlank()
                        ? request.getStatus()
                        : "NEW");
        order.setAppliedPromoCode(request.getStatus() != null && !request.getStatus().isBlank()
                ? request.getAppliedPromoCode()
                : null);
        order.setTotal(0.0);
        order.setLines(new ArrayList<>());

        for (OrderLineItemRequest line : request.getLines()) {
            appendLine(order, line);
        }
        order.recalculateTotal();
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAllWithLinesAndProducts();
    }

    /**
     * Paginated list with optional case-insensitive filter on status.
     * Sort via {@link Pageable} (e.g. sort=orderDate,desc).
     */
    @Transactional(readOnly = true)
    public Page<Order> findPage(String search, Pageable pageable) {
        if (search == null || search.isBlank()) {
            return orderRepository.findAll(pageable);
        }
        return orderRepository.findByStatusContainingIgnoreCase(search.trim(), pageable);
    }

    @Transactional(readOnly = true)
    public Order getById(Long id) {
        return orderRepository.findByIdWithLinesAndProducts(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
    }

    public Order update(Long id, OrderUpdateRequest request) {
        boolean hasStatus = request.getStatus() != null && !request.getStatus().isBlank();
        if (request.getLines() == null && !hasStatus) {
            throw new IllegalArgumentException("Provide at least status or lines to update");
        }

        Order order = orderRepository.findByIdWithLinesAndProducts(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));

        if (request.getLines() != null) {
            List<OrderLine> snapshot = new ArrayList<>(order.getLines());
            for (OrderLine line : snapshot) {
                restoreStock(line);
            }
            order.getLines().clear();
            for (OrderLineItemRequest line : request.getLines()) {
                appendLine(order, line);
            }
            order.recalculateTotal();
        }
        if (hasStatus) {
            order.setStatus(request.getStatus());
        }
        return orderRepository.save(order);
    }

    public void delete(Long id) {
        Order order = orderRepository.findByIdWithLinesAndProducts(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        for (OrderLine line : new ArrayList<>(order.getLines())) {
            restoreStock(line);
        }
        orderRepository.delete(order);
    }

    private void appendLine(Order order, OrderLineItemRequest item) {
        Product product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + item.getProductId()));
        if (product.getStock() < item.getQuantity()) {
            throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
        }
        product.setStock(product.getStock() - item.getQuantity());
        productRepository.save(product);

        OrderLine line = new OrderLine();
        line.setOrder(order);
        line.setProduct(product);
        line.setQuantity(item.getQuantity());
        line.setUnitPrice(product.getPrice());
        order.getLines().add(line);
    }

    private void restoreStock(OrderLine line) {
        Product product = line.getProduct();
        product.setStock(product.getStock() + line.getQuantity());
        productRepository.save(product);
    }
}
