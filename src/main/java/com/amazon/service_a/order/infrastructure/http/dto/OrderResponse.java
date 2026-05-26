package com.amazon.service_a.order.infrastructure.http.dto;

import java.math.BigDecimal;

public record OrderResponse(
        Long id,
        String name,
        BigDecimal amount,
        PaymentResponse payment
) {
}
