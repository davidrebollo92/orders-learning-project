package com.amazon.service_a.orders.domain;

public interface OrderEventPublisherPort {
    void publishOrderCreated(Order order);
}
