package com.amazon.order_service.order.domain;

public interface OrderEventPublisher {
    void publishOrderCreated(Order order);
}
