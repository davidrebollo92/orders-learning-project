package com.amazon.service_a.orders.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

// TODO moverlo a com.amazon.service_a.orders.infrastructure.persistence
public interface JpaOrderRepository extends JpaRepository<OrderEntity, Long> {
}
