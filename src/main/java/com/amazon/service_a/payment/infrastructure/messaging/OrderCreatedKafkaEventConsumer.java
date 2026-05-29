package com.amazon.service_a.payment.infrastructure.messaging;

import com.amazon.service_a.payment.aplication.PaymentProcessor;
import com.amazon.service_a.payment.domain.Payment;
import com.amazon.service_a.payment.domain.exception.PaymentAlreadyPaidException;
import com.amazon.service_a.shared.domain.vo.Money;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderCreatedKafkaEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedKafkaEventConsumer.class);

    private final PaymentProcessor paymentProcessor;

    @KafkaListener(topics = "#{@kafkaTopicsConfig.orders}", groupId = "payment-processor")
    public void consume(OrderCreatedEvent event) {
        try {
            Payment payment = Payment.create(event.paymentId(), Payment.State.PENDING);
            Money amount = new Money(event.amount());

            paymentProcessor.process(payment, amount);
        } catch (PaymentAlreadyPaidException ex) {
            log.warn("Duplicate OrderCreatedEvent received: {}", ex.getMessage());
        }
    }
}
