package com.amazon.service_b.payment.infrastructure.messaging;

import com.amazon.avro.OrderCreatedEvent;
import com.amazon.service_b.payment.aplication.PaymentProcessor;
import com.amazon.service_b.payment.domain.Payment;
import com.amazon.service_b.payment.domain.exception.PaymentAlreadyPaidException;
import com.amazon.service_boot.core.domain.vo.Money;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderCreatedKafkaEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedKafkaEventConsumer.class);

    private final PaymentProcessor paymentProcessor;

    @RetryableTopic(
            attempts = "3",
            backOff = @BackOff(delay = 1000, multiplier = 2.0),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(topics = "#{@kafkaTopicsConfig.ordersCreated}", groupId = "payment-processor")
    public void consume(OrderCreatedEvent event) {
        try {
            Payment payment = Payment.create(UUID.fromString(event.getPaymentId()), UUID.fromString(event.getOrderId()));
            Money amount = new Money(new BigDecimal(event.getAmount()));

            paymentProcessor.process(payment, amount);
        } catch (PaymentAlreadyPaidException ex) {
            log.warn("Duplicate OrderCreatedEvent received: {}", ex.getMessage());
        }
    }

    @DltHandler
    public void handleDlt(OrderCreatedEvent event) {
        log.error("DLT: OrderCreatedEvent could not be processed after retries: {}", event);
    }
}
