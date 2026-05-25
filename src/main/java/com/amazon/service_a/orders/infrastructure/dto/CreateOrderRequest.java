package com.amazon.service_a.orders.infrastructure.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

// TODO moverlo a com.amazon.service_a.orders.infrastructure.http.dto
public record CreateOrderRequest(
        @NotBlank String name,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount
) {
}
