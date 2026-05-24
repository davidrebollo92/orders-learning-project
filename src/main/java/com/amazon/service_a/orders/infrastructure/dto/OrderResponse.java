package com.amazon.service_a.orders.infrastructure.dto;

import java.math.BigDecimal;

public record OrderResponse(
        Long id,
        String name,
        BigDecimal amount,
        PaymentResponse payment
) {
}
