package com.amazon.service_a.orders.infrastructure.dto;

import java.util.List;

// TODO Esto te sobra. Tiene sentido cuando se pagina el endpoint, no es tu caso ahora.
public record OrderListResponse(
        int count,
        List<OrderResponse> orders
) {
}
