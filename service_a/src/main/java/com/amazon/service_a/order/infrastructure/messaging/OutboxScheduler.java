package com.amazon.service_a.order.infrastructure.messaging;

import com.amazon.service_a.order.infrastructure.persistence.JpaOutboxEventRepository;
import com.amazon.service_a.order.infrastructure.persistence.OutboxEventEntity;
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
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxScheduler(JpaOutboxEventRepository jpaOutboxEventRepository,
                           @Qualifier("outboxKafkaTemplate") KafkaTemplate<String, String> kafkaTemplate) {
        this.jpaOutboxEventRepository = jpaOutboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEventEntity> pending = jpaOutboxEventRepository.findByPublishedAtIsNull();

        for (OutboxEventEntity outboxEvent : pending) {
            kafkaTemplate.send(outboxEvent.getTopic(), outboxEvent.getAggregateId().toString(), outboxEvent.getPayload());

            outboxEvent.setPublishedAt(Instant.now());

            log.debug("Outbox event published: topic={} aggregateId={}", outboxEvent.getTopic(), outboxEvent.getAggregateId());
        }
    }
}
