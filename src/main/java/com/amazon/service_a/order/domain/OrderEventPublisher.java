package com.amazon.service_a.order.domain;

public interface OrderEventPublisher {
    void publishOrderCreated(Order order);
}
