package com.amazon.inventory_service.reservation.infrastructure.http.dto;

import java.util.UUID;

public record ReservationResponse(
        UUID id,
        UUID orderId,
        UUID productId,
        int quantity,
        String state
) {
}
