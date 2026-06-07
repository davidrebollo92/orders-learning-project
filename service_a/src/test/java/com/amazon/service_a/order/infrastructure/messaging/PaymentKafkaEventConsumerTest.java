package com.amazon.service_a.order.infrastructure.messaging;

import com.amazon.avro.PaymentCompletedEvent;
import com.amazon.avro.PaymentFailedEvent;
import com.amazon.service_a.order.aplication.OrderCanceller;
import com.amazon.service_a.order.aplication.PaymentCompleter;
import com.amazon.service_a.order.domain.exception.OrderNotFoundException;
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
class PaymentKafkaEventConsumerTest {

    @Mock
    private PaymentCompleter paymentCompleter;

    @Mock
    private OrderCanceller orderCanceller;

    @InjectMocks
    private PaymentKafkaEventConsumer consumer;

    private PaymentCompletedEvent completedEvent(UUID paymentId, UUID orderId) {
        return PaymentCompletedEvent.newBuilder()
                .setPaymentId(paymentId.toString())
                .setOrderId(orderId.toString())
                .build();
    }

    private PaymentFailedEvent failedEvent(UUID paymentId, UUID orderId) {
        return PaymentFailedEvent.newBuilder()
                .setPaymentId(paymentId.toString())
                .setOrderId(orderId.toString())
                .build();
    }

    @Test
    void consumeCompleted_callsPaymentCompleter() {
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        consumer.consumeCompleted(completedEvent(paymentId, orderId));

        verify(paymentCompleter).complete(orderId, paymentId);
    }

    @Test
    void consumeFailed_callsOrderCanceller() {
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        consumer.consumeFailed(failedEvent(paymentId, orderId));

        verify(orderCanceller).cancel(orderId, paymentId);
    }

    @Test
    void consumeCompleted_doesNotThrow_whenPaymentNotFound() {
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        doThrow(new PaymentNotFoundException(paymentId)).when(paymentCompleter).complete(orderId, paymentId);

        assertThatNoException().isThrownBy(() -> consumer.consumeCompleted(completedEvent(paymentId, orderId)));
    }

    @Test
    void consumeCompleted_doesNotThrow_whenOrderNotFound() {
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        doThrow(new OrderNotFoundException(orderId)).when(paymentCompleter).complete(orderId, paymentId);

        assertThatNoException().isThrownBy(() -> consumer.consumeCompleted(completedEvent(paymentId, orderId)));
    }

    @Test
    void consumeCompleted_doesNotThrow_whenPaymentAlreadyPaid() {
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        doThrow(new PaymentAlreadyPaidException(paymentId)).when(paymentCompleter).complete(orderId, paymentId);

        assertThatNoException().isThrownBy(() -> consumer.consumeCompleted(completedEvent(paymentId, orderId)));
    }

    @Test
    void consumeFailed_doesNotThrow_whenPaymentNotFound() {
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        doThrow(new PaymentNotFoundException(paymentId)).when(orderCanceller).cancel(orderId, paymentId);

        assertThatNoException().isThrownBy(() -> consumer.consumeFailed(failedEvent(paymentId, orderId)));
    }

    @Test
    void consumeFailed_doesNotThrow_whenOrderNotFound() {
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        doThrow(new OrderNotFoundException(orderId)).when(orderCanceller).cancel(orderId, paymentId);

        assertThatNoException().isThrownBy(() -> consumer.consumeFailed(failedEvent(paymentId, orderId)));
    }

    @Test
    void handleCompletedDlt_doesNotThrow() {
        assertThatNoException().isThrownBy(() ->
                consumer.handleCompletedDlt(completedEvent(UUID.randomUUID(), UUID.randomUUID())));
    }

    @Test
    void handleFailedDlt_doesNotThrow() {
        assertThatNoException().isThrownBy(() ->
                consumer.handleFailedDlt(failedEvent(UUID.randomUUID(), UUID.randomUUID())));
    }
}
