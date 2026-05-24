package com.amazon.service_a.orders.domain;

import java.util.List;
import java.util.Optional;

public interface OrderRepositoryPort {
    Order create(Order order);

    List<Order> getAll();

    Optional<Order> findById(Long id);
}
