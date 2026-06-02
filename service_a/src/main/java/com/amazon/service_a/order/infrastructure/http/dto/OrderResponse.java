package com.amazon.service_a.order.infrastructure.http.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String name,
        BigDecimal amount,
        PaymentResponse payment
) {
}
