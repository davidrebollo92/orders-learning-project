package com.amazon.service_b.payment.infrastructure.messaging;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderCreatedEvent(String type, UUID orderId, BigDecimal amount, UUID paymentId) {
}
