package com.amazon.service_a.order.infrastructure.messaging;

import java.util.UUID;

public record PaymentCompletedEvent(UUID orderId, UUID paymentId) {
}
