package com.amazon.service_a.order.infrastructure.messaging;

import com.amazon.avro.PaymentCompletedEvent;
import com.amazon.avro.PaymentFailedEvent;
import com.amazon.service_a.order.aplication.OrderCanceller;
import com.amazon.service_a.order.aplication.PaymentCompleter;
import com.amazon.service_a.order.domain.exception.OrderNotFoundException;
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

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentKafkaEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentKafkaEventConsumer.class);

    private final PaymentCompleter paymentCompleter;
    private final OrderCanceller orderCanceller;

    @RetryableTopic(
            attempts = "3",
            backOff = @BackOff(delay = 1000, multiplier = 2.0),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(topics = "#{@kafkaTopicsConfig.paymentsCompleted}", groupId = "order-payment-updater")
    public void consumeCompleted(PaymentCompletedEvent event) {
        try {
            paymentCompleter.complete(UUID.fromString(event.getOrderId()), UUID.fromString(event.getPaymentId()));
        } catch (PaymentNotFoundException ex) {
            log.error("PaymentCompletedEvent received for unknown payment: {}", ex.getMessage());
        } catch (OrderNotFoundException ex) {
            log.error("PaymentCompletedEvent received for unknown order: {}", ex.getMessage());
        } catch (PaymentAlreadyPaidException ex) {
            log.warn("Duplicate PaymentCompletedEvent received: {}", ex.getMessage());
        }
    }

    @RetryableTopic(
            attempts = "3",
            backOff = @BackOff(delay = 1000, multiplier = 2.0),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(topics = "#{@kafkaTopicsConfig.paymentsFailed}", groupId = "order-payment-updater")
    public void consumeFailed(PaymentFailedEvent event) {
        try {
            orderCanceller.cancel(UUID.fromString(event.getOrderId()), UUID.fromString(event.getPaymentId()));
        } catch (PaymentNotFoundException ex) {
            log.error("PaymentFailedEvent received for unknown payment: {}", ex.getMessage());
        } catch (OrderNotFoundException ex) {
            log.error("PaymentFailedEvent received for unknown order: {}", ex.getMessage());
        }
    }

    @DltHandler
    public void handleCompletedDlt(PaymentCompletedEvent event) {
        log.error("DLT: PaymentCompletedEvent could not be processed after retries: {}", event);
    }

    @DltHandler
    public void handleFailedDlt(PaymentFailedEvent event) {
        log.error("DLT: PaymentFailedEvent could not be processed after retries: {}", event);
    }
}
