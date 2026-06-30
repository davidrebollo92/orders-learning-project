package com.amazon.order_service.order.domain.exception;

import java.util.UUID;

public class ProductNotFoundException extends OrderDomainException {
    public ProductNotFoundException(UUID id) {
        super("Product not found with id: " + id, "PRODUCT_NOT_FOUND");
    }
}
