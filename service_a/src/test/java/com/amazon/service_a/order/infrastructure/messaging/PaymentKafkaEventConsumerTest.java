package com.amazon.service_a.order.infrastructure.messaging;

import com.amazon.service_a.order.aplication.OrderCanceller;
import com.amazon.service_a.order.aplication.PaymentCompleter;
import com.amazon.service_a.order.domain.exception.OrderNotFoundException;
import com.amazon.service_a.order.domain.exception.PaymentAlreadyPaidException;
import com.amazon.service_a.order.domain.exception.PaymentNotFoundException;
import com.amazon.service_a.order.infrastructure.messaging.dto.PaymentEvent;
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
class PaymentKafkaEventConsumerTest {

    @Mock
    private PaymentCompleter paymentCompleter;

    @Mock
    private OrderCanceller orderCanceller;

    @InjectMocks
    private PaymentKafkaEventConsumer consumer;

    @Test
    void consume_callsPaymentCompleter_whenPaymentCompleted() {
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        PaymentEvent event = new PaymentEvent("PAYMENT_COMPLETED", paymentId, orderId);

        consumer.consume(event);

        verify(paymentCompleter).complete(orderId, paymentId);
    }

    @Test
    void consume_callsOrderCanceller_whenPaymentFailed() {
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        PaymentEvent event = new PaymentEvent("PAYMENT_FAILED", paymentId, orderId);

        consumer.consume(event);

        verify(orderCanceller).cancel(orderId, paymentId);
    }

    @Test
    void consume_doesNotThrow_whenPaymentNotFound() {
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        PaymentEvent event = new PaymentEvent("PAYMENT_COMPLETED", paymentId, orderId);
        doThrow(new PaymentNotFoundException(paymentId)).when(paymentCompleter).complete(orderId, paymentId);

        assertThatNoException().isThrownBy(() -> consumer.consume(event));
    }

    @Test
    void consume_doesNotThrow_whenOrderNotFound() {
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        PaymentEvent event = new PaymentEvent("PAYMENT_FAILED", paymentId, orderId);
        doThrow(new OrderNotFoundException(orderId)).when(orderCanceller).cancel(orderId, paymentId);

        assertThatNoException().isThrownBy(() -> consumer.consume(event));
    }

    @Test
    void consume_doesNotThrow_whenPaymentAlreadyPaid() {
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        PaymentEvent event = new PaymentEvent("PAYMENT_COMPLETED", paymentId, orderId);
        doThrow(new PaymentAlreadyPaidException(paymentId)).when(paymentCompleter).complete(orderId, paymentId);

        assertThatNoException().isThrownBy(() -> consumer.consume(event));
    }

    @Test
    void consume_doesNotThrow_whenUnknownType() {
        PaymentEvent event = new PaymentEvent("UNKNOWN", UUID.randomUUID(), UUID.randomUUID());

        assertThatNoException().isThrownBy(() -> consumer.consume(event));
    }

    @Test
    void handleDlt_doesNotThrow() {
        PaymentEvent event = new PaymentEvent("PAYMENT_COMPLETED", UUID.randomUUID(), UUID.randomUUID());

        assertThatNoException().isThrownBy(() -> consumer.handleDlt(event));
    }
}
