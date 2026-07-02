package com.amazon.order_service.order.domain.exception;

public class InsufficientStockException extends OrderDomainException {
    public InsufficientStockException() {
        super("Insufficient stock available", "INSUFFICIENT_STOCK");
    }
}
