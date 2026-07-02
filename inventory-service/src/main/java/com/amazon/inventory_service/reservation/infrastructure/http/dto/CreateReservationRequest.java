package com.amazon.inventory_service.reservation.infrastructure.http.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateReservationRequest(
        @NotNull UUID orderId,
        @NotNull UUID productId,
        @Min(1) int quantity
) {
}
