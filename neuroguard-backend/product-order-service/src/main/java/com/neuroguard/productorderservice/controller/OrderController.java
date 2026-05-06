package com.neuroguard.productorderservice.controller;

import com.neuroguard.productorderservice.dto.OrderCreateRequest;
import com.neuroguard.productorderservice.dto.OrderUpdateRequest;
import com.neuroguard.productorderservice.entity.Order;
import com.neuroguard.productorderservice.service.OrderService;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    public Order create(@Valid @RequestBody OrderCreateRequest request) {
        return orderService.create(request);
    }

    /**
     * Paginated orders: {@code ?page=0&size=10&sort=orderDate,desc&search=PENDING}
     * Search filters by status (contains, case-insensitive).
     */
    @GetMapping
    public Page<Order> page(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return orderService.findPage(search, pageable);
    }

    @GetMapping("/{id}")
    public Order getById(@PathVariable Long id) {
        return orderService.getById(id);
    }

    @GetMapping("/internal/{id}")
    public Order getInternalById(@PathVariable Long id) {
        return orderService.getById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Order update(@PathVariable Long id, @Valid @RequestBody OrderUpdateRequest request) {
        return orderService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        orderService.delete(id);
    }
}
