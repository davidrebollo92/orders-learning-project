package com.amazon.inventory_service.reservation.infrastructure.persistence;

import com.amazon.inventory_service.reservation.infrastructure.persistence.entity.DeadLetterEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaDeadLetterEventRepository extends JpaRepository<DeadLetterEventEntity, UUID> {
}
