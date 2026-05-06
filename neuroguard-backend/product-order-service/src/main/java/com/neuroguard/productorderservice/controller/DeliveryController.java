package com.neuroguard.productorderservice.controller;

import com.neuroguard.productorderservice.dto.DeliveryCreateRequest;
import com.neuroguard.productorderservice.dto.DeliveryUpdateRequest;
import com.neuroguard.productorderservice.entity.Delivery;
import com.neuroguard.productorderservice.service.DeliveryService;
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
@RequestMapping("/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping("/{orderId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public Delivery create(
            @PathVariable Long orderId,
            @Valid @RequestBody DeliveryCreateRequest request) {
        return deliveryService.create(orderId, request);
    }

    @GetMapping
    public Page<Delivery> list(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "deliveryDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return deliveryService.findPage(search, pageable);
    }

    @GetMapping("/{id}")
    public Delivery getById(@PathVariable Long id) {
        return deliveryService.getById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Delivery update(@PathVariable Long id, @Valid @RequestBody DeliveryUpdateRequest request) {
        return deliveryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        deliveryService.delete(id);
    }
}
