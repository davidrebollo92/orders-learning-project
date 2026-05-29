package com.amazon.service_a.order.infrastructure.messaging;

import com.amazon.service_a.order.aplication.PaymentCompleter;
import com.amazon.service_a.order.domain.exception.PaymentAlreadyCompletedException;
import com.amazon.service_a.order.domain.exception.PaymentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentCompletedKafkaEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentCompletedKafkaEventConsumer.class);

    private final PaymentCompleter paymentCompleter;

    @KafkaListener(topics = "#{@kafkaTopicsConfig.payments}", groupId = "order-payment-updater")
    public void consume(PaymentCompletedEvent event) {
        try {
            paymentCompleter.complete(event.paymentId());
        } catch (PaymentNotFoundException ex) {
            log.error("PaymentCompletedEvent received for unknown payment: {}", ex.getMessage());
        } catch (PaymentAlreadyCompletedException ex) {
            log.warn("Duplicate PaymentCompletedEvent received: {}", ex.getMessage());
        }
    }
}
