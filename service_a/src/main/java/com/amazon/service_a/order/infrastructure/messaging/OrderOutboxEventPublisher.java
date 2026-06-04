package com.amazon.service_a.order.infrastructure.messaging;

import com.amazon.service_a.order.domain.Order;
import com.amazon.service_a.order.domain.OrderEventPublisher;
import com.amazon.service_a.order.infrastructure.persistence.JpaOutboxEventRepository;
import com.amazon.service_a.order.infrastructure.persistence.OutboxEventEntity;
import com.amazon.service_boot.core.infrastructure.messaging.KafkaTopicsConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderOutboxEventPublisher implements OrderEventPublisher {

    private final JpaOutboxEventRepository jpaOutboxEventRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTopicsConfig kafkaTopicsConfig;

    @Override
    public void publishOrderCreated(Order order) {
        OrderCreatedEvent event = new OrderCreatedEvent(order.id(), order.amount().amount(), order.payment().id());

        try {
            String payload = objectMapper.writeValueAsString(event);

            jpaOutboxEventRepository.save(new OutboxEventEntity(
                    UUID.randomUUID(),
                    order.id(),
                    kafkaTopicsConfig.getOrders(),
                    payload,
                    Instant.now(),
                    null
            ));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize OrderCreatedEvent", e);
        }
    }
}
