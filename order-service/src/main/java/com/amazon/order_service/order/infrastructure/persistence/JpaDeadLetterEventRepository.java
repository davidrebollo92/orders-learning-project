package com.amazon.order_service.order.infrastructure.persistence;

import com.amazon.order_service.order.infrastructure.persistence.entity.DeadLetterEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaDeadLetterEventRepository extends JpaRepository<DeadLetterEventEntity, UUID> {
}
