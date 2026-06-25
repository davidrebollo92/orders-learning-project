package com.amazon.order_service.order.infrastructure.persistence;

import com.amazon.order_service.order.domain.Payment;
import com.amazon.order_service.order.infrastructure.persistence.entity.OrderPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface JpaOrderPaymentRepository extends JpaRepository<OrderPaymentEntity, UUID> {

    @Modifying
    @Query("UPDATE OrderPaymentEntity p SET p.state = :state WHERE p.id = :id")
    void updateState(@Param("id") UUID id, @Param("state") Payment.State state);
}
