package com.amazon.service_a.orders.infrastructure.dto;

import java.math.BigDecimal;

// TODO moverlo a com.amazon.service_a.orders.infrastructure.http.dto
public record OrderResponse(
        Long id,
        String name,
        BigDecimal amount,
        PaymentResponse payment
) {
}
