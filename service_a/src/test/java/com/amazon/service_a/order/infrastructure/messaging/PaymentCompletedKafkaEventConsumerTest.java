package com.amazon.service_a.order.infrastructure.messaging;

import com.amazon.service_a.order.aplication.PaymentCompleter;
import com.amazon.service_a.order.domain.exception.PaymentAlreadyPaidException;
import com.amazon.service_a.order.domain.exception.PaymentNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentCompletedKafkaEventConsumerTest {

    @Mock
    private PaymentCompleter paymentCompleter;

    @InjectMocks
    private PaymentCompletedKafkaEventConsumer consumer;

    @Test
    void consume_callsPaymentCompleter() {
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        PaymentCompletedEvent event = new PaymentCompletedEvent(orderId, paymentId);

        consumer.consume(event);

        verify(paymentCompleter).complete(orderId, paymentId);
    }

    @Test
    void consume_doesNotThrow_whenPaymentNotFound() {
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        PaymentCompletedEvent event = new PaymentCompletedEvent(orderId, paymentId);
        doThrow(new PaymentNotFoundException(paymentId)).when(paymentCompleter).complete(orderId, paymentId);

        assertThatNoException().isThrownBy(() -> consumer.consume(event));
    }

    @Test
    void consume_doesNotThrow_whenPaymentAlreadyPaid() {
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        PaymentCompletedEvent event = new PaymentCompletedEvent(orderId, paymentId);
        doThrow(new PaymentAlreadyPaidException(paymentId)).when(paymentCompleter).complete(orderId, paymentId);

        assertThatNoException().isThrownBy(() -> consumer.consume(event));
    }

    @Test
    void handleDlt_doesNotThrow() {
        PaymentCompletedEvent event = new PaymentCompletedEvent(UUID.randomUUID(), UUID.randomUUID());

        assertThatNoException().isThrownBy(() -> consumer.handleDlt(event));
    }
}
