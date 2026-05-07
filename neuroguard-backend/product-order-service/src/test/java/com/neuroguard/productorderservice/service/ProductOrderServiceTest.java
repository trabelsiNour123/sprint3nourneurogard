package com.neuroguard.productorderservice.service;

import com.neuroguard.productorderservice.dto.ProductRequest;
import com.neuroguard.productorderservice.entity.Product;
import com.neuroguard.productorderservice.exception.ResourceNotFoundException;
import com.neuroguard.productorderservice.repository.OrderLineRepository;
import com.neuroguard.productorderservice.repository.ProductRepository;
import com.neuroguard.productorderservice.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductOrderServiceTest {

    private ProductRepository productRepository;
    private OrderLineRepository orderLineRepository;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        orderLineRepository = mock(OrderLineRepository.class);
        productService = new ProductService(productRepository, orderLineRepository);
    }

    @Test
    void createProduct_success() {
        Product p = new Product();
        p.setId(1L);
        p.setName("Test Product");
        p.setPrice(9.99);
        p.setStock(10);

        ProductRequest req = new ProductRequest();
        req.setName("Test Product");
        req.setPrice(9.99);
        req.setStock(10);

        when(productRepository.save(any(Product.class))).thenReturn(p);

        var created = productService.create(req);

        assertNotNull(created);
        assertEquals(1L, created.getId());
        assertEquals("Test Product", created.getName());
    }

    @Test
    void getProductById_notFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.getById(99L));
    }
}
