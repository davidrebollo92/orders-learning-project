package com.amazon.service_b.payment.infrastructure.messaging;

import java.util.UUID;

public record PaymentCompletedEvent(UUID paymentId, UUID orderId) {
}
