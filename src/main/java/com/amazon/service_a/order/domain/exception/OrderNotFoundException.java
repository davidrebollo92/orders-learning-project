package com.amazon.service_a.order.domain.exception;

public class OrderNotFoundException extends OrderDomainException {

    public OrderNotFoundException(Long id) {
        super("Order not found with id: " + id, "ORDER_NOT_FOUND");
    }
}
