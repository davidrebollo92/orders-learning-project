package com.amazon.service_a.order.infrastructure.messaging.dto;

import java.util.UUID;

// Wrapper temporal: sustituir por PaymentCompletedEvent y PaymentFailedEvent al implementar Schema Registry.
public record PaymentEvent(String type, UUID paymentId, UUID orderId) {
}
