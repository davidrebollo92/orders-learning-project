package com.amazon.service_a.order.domain;

public interface OrderEventPublisherPort {
    void publishOrderCreated(Order order);
}
