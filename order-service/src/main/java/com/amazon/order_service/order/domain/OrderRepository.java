package com.amazon.order_service.order.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    Order save(Order order);

    List<Order> getAll();

    Optional<Order> findById(UUID id);

    void update(Order order);
}
