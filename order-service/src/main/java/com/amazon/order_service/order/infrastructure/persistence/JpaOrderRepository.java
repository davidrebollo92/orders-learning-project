package com.amazon.order_service.order.infrastructure.persistence;

import com.amazon.order_service.order.domain.Order;
import com.amazon.order_service.order.infrastructure.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface JpaOrderRepository extends JpaRepository<OrderEntity, UUID> {

    @Modifying
    @Query("UPDATE OrderEntity o SET o.state = :state WHERE o.id = :id")
    void updateState(@Param("id") UUID id, @Param("state") Order.State state);
}
