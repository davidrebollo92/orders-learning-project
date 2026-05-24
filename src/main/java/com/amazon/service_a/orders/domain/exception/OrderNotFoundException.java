package com.amazon.service_a.orders.domain.exception;

public class OrderNotFoundException extends OrderDomainException {

    public OrderNotFoundException(Long id) {
        super("Order not found with id: " + id);
    }
}
