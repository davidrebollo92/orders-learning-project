package com.amazon.service_a.order.infrastructure.messaging.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderCreatedEvent(String type, UUID orderId, BigDecimal amount, UUID paymentId) {

    public OrderCreatedEvent(UUID orderId, BigDecimal amount, UUID paymentId) {
        this("ORDER_CREATED", orderId, amount, paymentId);
    }
}
