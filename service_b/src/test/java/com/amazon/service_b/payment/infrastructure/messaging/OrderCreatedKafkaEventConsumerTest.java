package com.amazon.service_b.payment.infrastructure.messaging;

import com.amazon.service_b.payment.aplication.PaymentProcessor;
import com.amazon.service_b.payment.domain.exception.PaymentAlreadyPaidException;
import com.amazon.service_b.payment.infrastructure.messaging.dto.OrderCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderCreatedKafkaEventConsumerTest {

    @Mock
    private PaymentProcessor paymentProcessor;

    @InjectMocks
    private OrderCreatedKafkaEventConsumer consumer;

    @Test
    void consume_callsPaymentProcessor() {
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        OrderCreatedEvent event = new OrderCreatedEvent("ORDER_CREATED", orderId, new BigDecimal("50.00"), paymentId);

        consumer.consume(event);

        verify(paymentProcessor).process(any(), any());
    }

    @Test
    void consume_doesNotThrow_whenPaymentAlreadyPaid() {
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        OrderCreatedEvent event = new OrderCreatedEvent("ORDER_CREATED", orderId, new BigDecimal("50.00"), paymentId);
        doThrow(new PaymentAlreadyPaidException(paymentId)).when(paymentProcessor).process(any(), any());

        assertThatNoException().isThrownBy(() -> consumer.consume(event));
    }

    @Test
    void handleDlt_doesNotThrow() {
        OrderCreatedEvent event = new OrderCreatedEvent("ORDER_CREATED", UUID.randomUUID(), new BigDecimal("50.00"), UUID.randomUUID());

        assertThatNoException().isThrownBy(() -> consumer.handleDlt(event));
    }
}
