package com.amazon.service_a.order.infrastructure.messaging;

import java.util.UUID;

public record PaymentCompletedEvent(String type, UUID paymentId) {}
