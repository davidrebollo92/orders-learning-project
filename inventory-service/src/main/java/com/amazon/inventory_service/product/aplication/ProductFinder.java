package com.amazon.inventory_service.product.aplication;

import com.amazon.inventory_service.product.domain.Product;
import com.amazon.inventory_service.product.domain.ProductRepository;
import com.amazon.inventory_service.product.domain.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductFinder {
    private final ProductRepository productRepository;

    public Product findById(UUID id) {
        return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }
}
