package com.amazon.service_b.payment.infrastructure.messaging;

import com.amazon.avro.OrderCreatedEvent;
import com.amazon.service_b.payment.aplication.PaymentProcessor;
import com.amazon.service_b.payment.domain.exception.PaymentAlreadyPaidException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    private OrderCreatedEvent event(UUID orderId, UUID paymentId) {
        return OrderCreatedEvent.newBuilder()
                .setOrderId(orderId.toString())
                .setAmount("50.00")
                .setPaymentId(paymentId.toString())
                .build();
    }

    @Test
    void consume_callsPaymentProcessor() {
        consumer.consume(event(UUID.randomUUID(), UUID.randomUUID()));

        verify(paymentProcessor).process(any(), any());
    }

    @Test
    void consume_doesNotThrow_whenPaymentAlreadyPaid() {
        doThrow(new PaymentAlreadyPaidException(UUID.randomUUID())).when(paymentProcessor).process(any(), any());

        assertThatNoException().isThrownBy(() -> consumer.consume(event(UUID.randomUUID(), UUID.randomUUID())));
    }

    @Test
    void handleDlt_doesNotThrow() {
        assertThatNoException().isThrownBy(() -> consumer.handleDlt(event(UUID.randomUUID(), UUID.randomUUID())));
    }
}
