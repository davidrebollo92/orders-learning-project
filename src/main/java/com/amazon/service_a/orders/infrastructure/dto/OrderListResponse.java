package com.amazon.service_a.orders.infrastructure.dto;

import java.util.List;

public record OrderListResponse(
        int count,
        List<OrderResponse> orders
) {
}
