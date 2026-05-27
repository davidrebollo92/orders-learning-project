package com.amazon.service_a.order.infrastructure.persistence;

import com.amazon.service_a.order.domain.Order;
import com.amazon.service_a.order.domain.OrderRepositoryPort;
import com.amazon.service_a.order.infrastructure.persistence.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
// TODO lo mismo del sufijo que te he dicho para los puertos
//  aqui representa más el tipo de adaptador, por ejemplo OrderPostgresSqlRepository
public class OrderRepositoryAdapter implements OrderRepositoryPort {

    private final JpaOrderRepository jpaOrderRepository;

    @Override
    public Order create(Order order) {
        OrderEntity entity = OrderMapper.toEntity(order);

        OrderEntity created = jpaOrderRepository.save(entity);

        return OrderMapper.toDomain(created);
    }

    @Override
    public List<Order> getAll() {
        return jpaOrderRepository.findAll().stream()
                .map(OrderMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Order> findById(Long id) {
        return jpaOrderRepository.findById(id)
                .map(OrderMapper::toDomain);
    }
}
