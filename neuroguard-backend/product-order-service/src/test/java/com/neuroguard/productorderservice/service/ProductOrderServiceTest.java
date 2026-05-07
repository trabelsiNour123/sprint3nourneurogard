package com.neuroguard.productorderservice.service;

import com.neuroguard.productorderservice.entity.Product;
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
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        productService = new ProductService(productRepository);
    }

    @Test
    void createProduct_success() {
        Product p = new Product();
        p.setId(1L);
        p.setName("Test Product");
        p.setPrice(new BigDecimal("9.99"));

        when(productRepository.save(any(Product.class))).thenReturn(p);

        var created = productService.createProduct(p);

        assertNotNull(created);
        assertEquals(1L, created.getId());
        assertEquals("Test Product", created.getName());
    }

    @Test
    void getProductById_notFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> productService.getProductById(99L));
    }
}
