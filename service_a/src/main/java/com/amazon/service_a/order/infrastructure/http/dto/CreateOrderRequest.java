package com.amazon.service_a.order.infrastructure.http.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateOrderRequest(
        @NotBlank String name,
        @NotNull BigDecimal amount
) {
}
