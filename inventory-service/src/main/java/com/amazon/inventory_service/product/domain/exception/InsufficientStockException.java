package com.amazon.inventory_service.product.domain.exception;

public class InsufficientStockException extends ProductDomainException {
    public InsufficientStockException() {
        super("Insufficient stock available", "INSUFFICIENT_STOCK");
    }
}
