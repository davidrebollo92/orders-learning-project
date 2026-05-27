package com.amazon.service_a.order.domain;

import java.util.List;
import java.util.Optional;

//TODO el sufijo Port está bien ahora para que entiendas el concepto, si lo tienes claro puede dejarlo como OrderRepository
public interface OrderRepositoryPort {
    Order create(Order order);

    List<Order> getAll();

    Optional<Order> findById(Long id);
}
