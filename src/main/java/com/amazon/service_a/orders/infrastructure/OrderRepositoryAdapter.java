package com.amazon.service_a.orders.infrastructure;

import org.springframework.stereotype.Repository;

import com.amazon.service_a.orders.domain.Order;
import com.amazon.service_a.orders.domain.OrderRepositoryPort;

import java.util.List;
import java.util.Optional;


@Repository
public class OrderRepositoryAdapter implements OrderRepositoryPort {

    private final JpaOrderRepository jpaRepository;

    public OrderRepositoryAdapter(JpaOrderRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }


    @Override
    public Order create(Order order) {
        OrderEntity entity = OrderMapper.toEntity(order);
        OrderEntity created = jpaRepository.save(entity);

        return OrderMapper.toDomain(created);
    }


    @Override
    public List<Order> getAll() {
        return jpaRepository.findAll().stream()
                .map(OrderMapper::toDomain).toList();
    }

    @Override
    public Optional<Order> findById(Long id) {
        return jpaRepository.findById(id)
                .map(OrderMapper::toDomain);
    }
}
