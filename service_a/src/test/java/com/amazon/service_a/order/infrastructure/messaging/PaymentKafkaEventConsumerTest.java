package com.amazon.service_a.order.infrastructure.messaging;

import com.amazon.avro.PaymentCompletedEvent;
import com.amazon.avro.PaymentFailedEvent;
import com.amazon.service_a.order.aplication.OrderCanceller;
import com.amazon.service_a.order.aplication.PaymentCompleter;
import com.amazon.service_a.order.domain.exception.OrderNotFoundException;
import com.amazon.service_a.order.domain.exception.PaymentAlreadyPaidException;
import com.amazon.service_a.order.domain.exception.PaymentNotFoundException;
import com.amazon.service_a.order.infrastructure.persistence.JpaDeadLetterEventRepository;
import com.amazon.service_a.order.infrastructure.persistence.entity.DeadLetterEventEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentKafkaEventConsumerTest {

    @Mock
    private PaymentCompleter paymentCompleter;

    @Mock
    private OrderCanceller orderCanceller;

    @Mock
    private JpaDeadLetterEventRepository jpaDeadLetterEventRepository;

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
    void handleDlt_savesPaymentCompletedEventToDeadLetterRepository() {
        PaymentCompletedEvent event = completedEvent(UUID.randomUUID(), UUID.randomUUID());

        consumer.handleDlt(event, "payments.completed-dlt", 0, 42L, "some error");

        ArgumentCaptor<DeadLetterEventEntity> captor = ArgumentCaptor.forClass(DeadLetterEventEntity.class);
        verify(jpaDeadLetterEventRepository).save(captor.capture());
        DeadLetterEventEntity saved = captor.getValue();
        assertThat(saved.getTopic()).isEqualTo("payments.completed-dlt");
        assertThat(saved.getEventType()).isEqualTo(PaymentCompletedEvent.class.getName());
        assertThat(saved.getExceptionMessage()).isEqualTo("some error");
        assertThat(saved.getOriginalPartition()).isEqualTo(0);
        assertThat(saved.getOriginalOffset()).isEqualTo(42L);
        assertThat(saved.getPayload()).isNotEmpty();
        assertThat(saved.getOccurredAt()).isNotNull();
    }

    @Test
    void handleDlt_savesPaymentFailedEventToDeadLetterRepository() {
        PaymentFailedEvent event = failedEvent(UUID.randomUUID(), UUID.randomUUID());

        consumer.handleDlt(event, "payments.failed-dlt", 0, 7L, "some error");

        ArgumentCaptor<DeadLetterEventEntity> captor = ArgumentCaptor.forClass(DeadLetterEventEntity.class);
        verify(jpaDeadLetterEventRepository).save(captor.capture());
        DeadLetterEventEntity saved = captor.getValue();
        assertThat(saved.getTopic()).isEqualTo("payments.failed-dlt");
        assertThat(saved.getEventType()).isEqualTo(PaymentFailedEvent.class.getName());
        assertThat(saved.getExceptionMessage()).isEqualTo("some error");
        assertThat(saved.getOriginalPartition()).isEqualTo(0);
        assertThat(saved.getOriginalOffset()).isEqualTo(7L);
        assertThat(saved.getPayload()).isNotEmpty();
        assertThat(saved.getOccurredAt()).isNotNull();
    }

    @Test
    void handleDlt_doesNotThrow_whenExceptionMessageIsNull() {
        assertThatNoException().isThrownBy(() ->
                consumer.handleDlt(completedEvent(UUID.randomUUID(), UUID.randomUUID()),
                        "topic-dlt", 0, 0L, null));
    }
}
