package com.amazon.payment_service.payment.infrastructure.messaging;

import com.amazon.avro.OrderCreatedEvent;
import com.amazon.payment_service.payment.aplication.PaymentCreator;
import com.amazon.payment_service.payment.domain.exception.PaymentAlreadyPaidException;
import com.amazon.payment_service.payment.infrastructure.persistence.JpaDeadLetterEventRepository;
import com.amazon.payment_service.payment.infrastructure.persistence.entity.DeadLetterEventEntity;
import com.amazon.shared.core.domain.vo.Money;
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
class OrderCreatedKafkaEventConsumerTest {

    @Mock
    private PaymentCreator paymentCreator;

    @Mock
    private JpaDeadLetterEventRepository jpaDeadLetterEventRepository;

    @InjectMocks
    private OrderCreatedKafkaEventConsumer consumer;

    private OrderCreatedEvent event(UUID orderId) {
        return OrderCreatedEvent.newBuilder()
                .setOrderId(orderId.toString())
                .setProductId(UUID.randomUUID().toString())
                .setQuantity(1)
                .setAmount("50.00")
                .build();
    }

    @Test
    void consume_callsPaymentCreator() {
        consumer.consume(event(UUID.randomUUID()));

        verify(paymentCreator).create(any(UUID.class), any(Money.class));
    }

    @Test
    void consume_doesNotThrow_whenPaymentAlreadyPaid() {
        doThrow(new PaymentAlreadyPaidException(UUID.randomUUID()))
                .when(paymentCreator).create(any(UUID.class), any(Money.class));

        assertThatNoException().isThrownBy(() -> consumer.consume(event(UUID.randomUUID())));
    }

    @Test
    void handleDlt_savesEventToDeadLetterRepository() {
        OrderCreatedEvent orderCreatedEvent = event(UUID.randomUUID());

        consumer.handleDlt(orderCreatedEvent, "orders.created-dlt", 0, 15L, "some error");

        ArgumentCaptor<DeadLetterEventEntity> captor = ArgumentCaptor.forClass(DeadLetterEventEntity.class);
        verify(jpaDeadLetterEventRepository).save(captor.capture());

        DeadLetterEventEntity saved = captor.getValue();
        assertThat(saved.getTopic()).isEqualTo("orders.created-dlt");
        assertThat(saved.getEventType()).isEqualTo(OrderCreatedEvent.class.getName());
        assertThat(saved.getExceptionMessage()).isEqualTo("some error");
        assertThat(saved.getOriginalPartition()).isEqualTo(0);
        assertThat(saved.getOriginalOffset()).isEqualTo(15L);
        assertThat(saved.getPayload()).isNotEmpty();
        assertThat(saved.getOccurredAt()).isNotNull();
    }

    @Test
    void handleDlt_doesNotThrow_whenExceptionMessageIsNull() {
        assertThatNoException().isThrownBy(() ->
                consumer.handleDlt(event(UUID.randomUUID()), "topic-dlt", 0, 0L, null));
    }
}
