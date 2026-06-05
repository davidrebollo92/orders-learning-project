package com.amazon.service_b.payment.infrastructure.messaging.dto;

import java.util.UUID;

public record PaymentFailedEvent(String type, UUID paymentId, UUID orderId) {
}
