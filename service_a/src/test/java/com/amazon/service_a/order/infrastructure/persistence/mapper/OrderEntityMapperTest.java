package com.amazon.service_a.order.infrastructure.persistence.mapper;

import com.amazon.service_a.order.domain.Order;
import com.amazon.service_a.order.domain.Payment;
import com.amazon.service_a.order.infrastructure.persistence.entity.OrderEntity;
import com.amazon.service_a.order.infrastructure.persistence.entity.OrderPaymentEntity;
import com.amazon.service_boot.core.domain.vo.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderEntityMapperTest {

    private final OrderPaymentEntityMapper paymentMapper = new OrderPaymentEntityMapper();
    private final OrderEntityMapper mapper = new OrderEntityMapper(paymentMapper);

    @Test
    void toEntity_mapsAllFields() {
        Order order = Order.create("laptop", new Money(new BigDecimal("10.00"))).addPayment();

        OrderEntity entity = mapper.toEntity(order);

        assertThat(entity.getId()).isEqualTo(order.id());
        assertThat(entity.getName()).isEqualTo("laptop");
        assertThat(entity.getAmount()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(entity.getPayment().getId()).isEqualTo(order.payment().id());
        assertThat(entity.getPayment().getState()).isEqualTo(Payment.State.PENDING);
    }

    @Test
    void toDomain_mapsAllFields() {
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        OrderPaymentEntity paymentEntity = new OrderPaymentEntity();
        paymentEntity.setId(paymentId);
        paymentEntity.setState(Payment.State.PAID);

        OrderEntity entity = new OrderEntity();
        entity.setId(orderId);
        entity.setName("laptop");
        entity.setAmount(new BigDecimal("10.00"));
        entity.setPayment(paymentEntity);

        Order order = mapper.toDomain(entity);

        assertThat(order.id()).isEqualTo(orderId);
        assertThat(order.name()).isEqualTo("laptop");
        assertThat(order.money()).isEqualTo(new Money(new BigDecimal("10.00")));
        assertThat(order.payment().id()).isEqualTo(paymentId);
        assertThat(order.payment().state()).isEqualTo(Payment.State.PAID);
    }
}
