package com.amazon.order_service.order.domain;

import java.util.UUID;

public interface StockReservationGateway {
    void reserve(UUID orderId, UUID productId, int quantity);
}
