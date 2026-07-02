package com.amazon.order_service.order.infrastructure.messaging;

import com.amazon.avro.OrderCreatedEvent;
import com.amazon.order_service.order.domain.Order;
import com.amazon.order_service.order.domain.OrderEventPublisher;
import com.amazon.order_service.order.infrastructure.persistence.JpaOutboxEventRepository;
import com.amazon.order_service.order.infrastructure.persistence.entity.OutboxEventEntity;
import com.amazon.shared.core.infrastructure.messaging.AvroUtils;
import com.amazon.shared.core.infrastructure.messaging.KafkaTopicsConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderOutboxEventPublisher implements OrderEventPublisher {

    private final JpaOutboxEventRepository jpaOutboxEventRepository;
    private final KafkaTopicsConfig kafkaTopicsConfig;

    @Override
    public void publishOrderCreated(Order order) {
        OrderCreatedEvent event = OrderCreatedEvent.newBuilder()
                .setOrderId(order.id().toString())
                .setProductId(order.productId().toString())
                .setQuantity(order.quantity())
                .setAmount(order.money().amount().toPlainString())
                .build();

        jpaOutboxEventRepository.save(new OutboxEventEntity(
                UUID.randomUUID(),
                order.id(),
                kafkaTopicsConfig.getOrdersCreated(),
                AvroUtils.toBytes(event),
                OrderCreatedEvent.class.getName(),
                Instant.now(),
                null
        ));
    }

}
