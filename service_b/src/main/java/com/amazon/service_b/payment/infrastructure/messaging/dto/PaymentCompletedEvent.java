package com.amazon.service_b.payment.infrastructure.messaging.dto;

import java.util.UUID;

public record PaymentCompletedEvent(String type, UUID paymentId, UUID orderId) {
}
