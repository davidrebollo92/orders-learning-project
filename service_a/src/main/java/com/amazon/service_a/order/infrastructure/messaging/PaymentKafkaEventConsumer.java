package com.amazon.service_a.order.infrastructure.messaging;

import com.amazon.service_a.order.aplication.OrderCanceller;
import com.amazon.service_a.order.aplication.PaymentCompleter;
import com.amazon.service_a.order.domain.exception.OrderNotFoundException;
import com.amazon.service_a.order.domain.exception.PaymentAlreadyPaidException;
import com.amazon.service_a.order.domain.exception.PaymentNotFoundException;
import com.amazon.service_a.order.infrastructure.messaging.dto.PaymentEvent;
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
public class PaymentKafkaEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentKafkaEventConsumer.class);

    private final PaymentCompleter paymentCompleter;
    private final OrderCanceller orderCanceller;

    @RetryableTopic(
            attempts = "3",
            backOff = @BackOff(delay = 1000, multiplier = 2.0),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(topics = "#{@kafkaTopicsConfig.payments}", groupId = "order-payment-updater")
    public void consume(PaymentEvent event) {
        try {
            switch (event.type()) {
                case "PAYMENT_COMPLETED" -> paymentCompleter.complete(event.orderId(), event.paymentId());
                case "PAYMENT_FAILED"    -> orderCanceller.cancel(event.orderId(), event.paymentId());
                default -> log.warn("Unknown payment event type: {}", event.type());
            }
        } catch (PaymentNotFoundException ex) {
            log.error("PaymentEvent received for unknown payment: {}", ex.getMessage());
        } catch (OrderNotFoundException ex) {
            log.error("PaymentEvent received for unknown order: {}", ex.getMessage());
        } catch (PaymentAlreadyPaidException ex) {
            log.warn("Duplicate PaymentEvent received: {}", ex.getMessage());
        }
    }

    @DltHandler
    public void handleDlt(PaymentEvent event) {
        log.error("DLT: PaymentEvent could not be processed after retries: {}", event);
    }
}
