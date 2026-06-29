package com.amazon.inventory_service.product.domain.exception;

import java.util.UUID;

public class ProductNotFoundException extends ProductDomainException {
    public ProductNotFoundException(UUID id) {
        super("Product not found with id: " + id, "PRODUCT_NOT_FOUND");
    }
}
