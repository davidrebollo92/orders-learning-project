package com.amazon.service_a.order.infrastructure.messaging;

import com.amazon.service_a.order.aplication.PaymentCompleter;
import com.amazon.service_a.order.domain.exception.PaymentAlreadyPaidException;
import com.amazon.service_a.order.domain.exception.PaymentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentCompletedKafkaEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentCompletedKafkaEventConsumer.class);

    private final PaymentCompleter paymentCompleter;

    @RetryableTopic(
            attempts = "3",
            backOff = @BackOff(delay = 1000, multiplier = 2.0),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(topics = "#{@kafkaTopicsConfig.payments}", groupId = "order-payment-updater")
    public void consume(PaymentCompletedEvent event) {
        try {
            paymentCompleter.complete(event.orderId(), event.paymentId());
        } catch (PaymentNotFoundException ex) {
            log.error("PaymentCompletedEvent received for unknown payment: {}", ex.getMessage());
        } catch (PaymentAlreadyPaidException ex) {
            log.warn("Duplicate PaymentCompletedEvent received: {}", ex.getMessage());
        }
    }

    @DltHandler
    public void handleDlt(PaymentCompletedEvent event) {
        log.error("DLT: PaymentCompletedEvent could not be processed after retries: {}", event);
    }
}
