package com.amazon.service_a.order.infrastructure.messaging;

import java.math.BigDecimal;

public record OrderCreatedEvent(Long orderId, BigDecimal amount, Long paymentId) {
}
