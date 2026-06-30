package com.amazon.payment_service.payment.infrastructure.messaging;

import com.amazon.payment_service.payment.infrastructure.persistence.JpaOutboxEventRepository;
import com.amazon.payment_service.payment.infrastructure.persistence.entity.OutboxEventEntity;
import com.amazon.shared.core.infrastructure.messaging.AvroUtils;
import org.apache.avro.specific.SpecificRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class OutboxScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxScheduler.class);

    private final JpaOutboxEventRepository jpaOutboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OutboxScheduler(JpaOutboxEventRepository jpaOutboxEventRepository,
                           @Qualifier("outboxKafkaTemplate") KafkaTemplate<String, Object> kafkaTemplate) {
        this.jpaOutboxEventRepository = jpaOutboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEventEntity> pending = jpaOutboxEventRepository.findByPublishedAtIsNull();

        for (OutboxEventEntity outboxEvent : pending) {
            try {
                SpecificRecord payload = AvroUtils.fromBytes(outboxEvent.getPayload(), outboxEvent.getEventType());

                kafkaTemplate.send(outboxEvent.getTopic(), outboxEvent.getAggregateId().toString(), payload);

                outboxEvent.setPublishedAt(Instant.now());

                log.debug("Outbox event published: topic={} aggregateId={}", outboxEvent.getTopic(), outboxEvent.getAggregateId());
            } catch (Exception e) {
                throw new RuntimeException("Failed to publish outbox event id=" + outboxEvent.getId(), e);
            }
        }
    }

}
