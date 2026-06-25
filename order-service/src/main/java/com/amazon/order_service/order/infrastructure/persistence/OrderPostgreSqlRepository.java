package com.amazon.order_service.order.infrastructure.persistence;

import com.amazon.order_service.order.domain.Order;
import com.amazon.order_service.order.domain.OrderRepository;
import com.amazon.order_service.order.infrastructure.persistence.entity.OrderEntity;
import com.amazon.order_service.order.infrastructure.persistence.mapper.OrderEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OrderPostgreSqlRepository implements OrderRepository {

    private final JpaOrderRepository jpaOrderRepository;
    private final JpaOrderPaymentRepository jpaOrderPaymentRepository;
    private final OrderEntityMapper orderEntityMapper;

    @Override
    public Order save(Order order) {
        OrderEntity entity = orderEntityMapper.toEntity(order);
        OrderEntity created = jpaOrderRepository.save(entity);

        return orderEntityMapper.toDomain(created);
    }

    @Override
    public List<Order> getAll() {
        return jpaOrderRepository.findAll().stream()
                .map(orderEntityMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return jpaOrderRepository.findById(id)
                .map(orderEntityMapper::toDomain);
    }

    @Override
    public void updatePayment(Order order) {
        jpaOrderRepository.updateState(order.id(), order.state());
        jpaOrderPaymentRepository.updateState(order.payment().id(), order.payment().state());
    }
}
