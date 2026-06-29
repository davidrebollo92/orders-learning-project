package com.amazon.inventory_service.product.aplication;

import com.amazon.inventory_service.product.domain.Product;
import com.amazon.inventory_service.product.domain.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductCreator {
    private final ProductRepository productRepository;

    public Product create(Product product) {
        return productRepository.save(product);
    }
}
