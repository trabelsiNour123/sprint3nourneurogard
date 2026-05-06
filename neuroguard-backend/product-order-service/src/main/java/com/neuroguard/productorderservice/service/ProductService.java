package com.neuroguard.productorderservice.service;

import com.neuroguard.productorderservice.dto.ProductRequest;
import com.neuroguard.productorderservice.entity.Product;
import com.neuroguard.productorderservice.exception.ResourceNotFoundException;
import com.neuroguard.productorderservice.repository.OrderLineRepository;
import com.neuroguard.productorderservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final OrderLineRepository orderLineRepository;

    public Product create(ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    /**
     * Paginated list with optional case-insensitive search on product name.
     * Sort via {@link Pageable} (e.g. sort=name,asc).
     */
    @Transactional(readOnly = true)
    public Page<Product> findPage(String search, Pageable pageable) {
        if (search == null || search.isBlank()) {
            return productRepository.findAll(pageable);
        }
        return productRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
    }

    @Transactional(readOnly = true)
    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    public Product update(Long id, ProductRequest request) {
        Product product = getById(id);
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        return productRepository.save(product);
    }

    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found: " + id);
        }
        if (orderLineRepository.existsByProduct_Id(id)) {
            throw new IllegalStateException("Cannot delete a product that appears on existing orders");
        }
        productRepository.deleteById(id);
    }
}
