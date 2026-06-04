package com.amazon.service_b.payment.infrastructure.messaging;

import com.amazon.service_b.payment.domain.Payment;
import com.amazon.service_b.payment.domain.PaymentEventPublisher;
import com.amazon.service_b.payment.infrastructure.persistence.JpaOutboxEventRepository;
import com.amazon.service_b.payment.infrastructure.persistence.OutboxEventEntity;
import com.amazon.service_boot.core.infrastructure.messaging.KafkaTopicsConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentOutboxEventPublisher implements PaymentEventPublisher {

    private final JpaOutboxEventRepository jpaOutboxEventRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTopicsConfig kafkaTopicsConfig;

    @Override
    public void publishPaymentCompleted(Payment payment) {
        PaymentCompletedEvent event = new PaymentCompletedEvent(payment.id(), payment.orderId());

        try {
            String payload = objectMapper.writeValueAsString(event);

            jpaOutboxEventRepository.save(new OutboxEventEntity(
                    UUID.randomUUID(),
                    payment.id(),
                    kafkaTopicsConfig.getPayments(),
                    payload,
                    Instant.now(),
                    null
            ));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize PaymentCompletedEvent", e);
        }
    }
}
