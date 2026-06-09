package com.amazon.service_a.order.infrastructure.persistence.mapper;

import com.amazon.service_a.order.domain.Payment;
import com.amazon.service_a.order.infrastructure.persistence.entity.OrderPaymentEntity;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderPaymentEntityMapperTest {

    private final OrderPaymentEntityMapper mapper = new OrderPaymentEntityMapper();

    @Test
    void toEntity_mapsIdAndState() {
        Payment payment = new Payment(UUID.randomUUID(), Payment.State.PENDING);

        OrderPaymentEntity entity = mapper.toEntity(payment);

        assertThat(entity.getId()).isEqualTo(payment.id());
        assertThat(entity.getState()).isEqualTo(Payment.State.PENDING);
    }

    @Test
    void toEntity_mapsPaidState() {
        Payment payment = new Payment(UUID.randomUUID(), Payment.State.PAID);

        OrderPaymentEntity entity = mapper.toEntity(payment);

        assertThat(entity.getState()).isEqualTo(Payment.State.PAID);
    }

    @Test
    void toDomain_mapsPendingState() {
        UUID id = UUID.randomUUID();
        OrderPaymentEntity entity = new OrderPaymentEntity();
        entity.setId(id);
        entity.setState(Payment.State.PENDING);

        Payment payment = mapper.toDomain(entity);

        assertThat(payment.id()).isEqualTo(id);
        assertThat(payment.state()).isEqualTo(Payment.State.PENDING);
    }

    @Test
    void toDomain_mapsPaidState() {
        UUID id = UUID.randomUUID();
        OrderPaymentEntity entity = new OrderPaymentEntity();
        entity.setId(id);
        entity.setState(Payment.State.PAID);

        Payment payment = mapper.toDomain(entity);

        assertThat(payment.id()).isEqualTo(id);
        assertThat(payment.state()).isEqualTo(Payment.State.PAID);
    }
}
