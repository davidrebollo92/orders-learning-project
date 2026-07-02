package com.amazon.inventory_service.reservation.domain;

import java.util.Optional;
import java.util.UUID;

public interface OrderGateway {
    Optional<OrderStatus> findStatus(UUID orderId);
}
