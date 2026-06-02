package com.amazon.service_a.order.infrastructure.messaging;

import com.amazon.service_a.order.domain.Order;
import com.amazon.service_a.order.domain.OrderEventPublisher;
import com.amazon.service_boot.core.infrastructure.messaging.KafkaTopicsConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderKafkaEventPublisher implements OrderEventPublisher {

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    private final KafkaTopicsConfig kafkaTopicsConfig;

    @Override
    public void publishOrderCreated(Order order) {
        OrderCreatedEvent event = new OrderCreatedEvent(
                order.id(),
                order.amount().amount(),
                order.payment().id()
        );

        kafkaTemplate.send(kafkaTopicsConfig.getOrders(), order.id().toString(), event);
    }
}
