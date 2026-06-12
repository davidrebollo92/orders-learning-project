package com.amazon.service_b.payment.infrastructure.persistence;

import com.amazon.service_b.payment.infrastructure.persistence.entity.DeadLetterEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaDeadLetterEventRepository extends JpaRepository<DeadLetterEventEntity, UUID> {
}
