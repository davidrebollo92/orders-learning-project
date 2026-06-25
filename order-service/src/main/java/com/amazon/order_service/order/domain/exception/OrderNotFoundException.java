package com.amazon.order_service.order.domain.exception;

import java.util.UUID;

public class OrderNotFoundException extends OrderDomainException {

    public OrderNotFoundException(UUID id) {
        super("Order not found with id: " + id, "ORDER_NOT_FOUND");
    }
}
