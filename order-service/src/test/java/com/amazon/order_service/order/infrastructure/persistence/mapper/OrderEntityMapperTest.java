package com.amazon.order_service.order.infrastructure.persistence.mapper;

import com.amazon.order_service.order.domain.Order;
import com.amazon.order_service.order.domain.Payment;
import com.amazon.order_service.order.infrastructure.persistence.entity.OrderEntity;
import com.amazon.shared.core.domain.vo.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderEntityMapperTest {

    private final OrderEntityMapper mapper = new OrderEntityMapper();

    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final int QUANTITY = 2;

    @Test
    void toEntity_mapsAllFields() {
        UUID paymentId = UUID.randomUUID();
        Order order = Order.create(PRODUCT_ID, QUANTITY, new Money(new BigDecimal("10.00"))).markPaid(paymentId);

        OrderEntity entity = mapper.toEntity(order);

        assertThat(entity.getId()).isEqualTo(order.id());
        assertThat(entity.getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(entity.getQuantity()).isEqualTo(QUANTITY);
        assertThat(entity.getAmount()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(entity.getState()).isEqualTo(Order.State.PAID);
        assertThat(entity.getPaymentId()).isEqualTo(paymentId);
        assertThat(entity.getPaymentState()).isEqualTo(Payment.State.PAID);
    }

    @Test
    void toDomain_mapsAllFields() {
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        OrderEntity entity = new OrderEntity();
        entity.setId(orderId);
        entity.setProductId(PRODUCT_ID);
        entity.setQuantity(QUANTITY);
        entity.setAmount(new BigDecimal("10.00"));
        entity.setState(Order.State.PAID);
        entity.setPaymentId(paymentId);
        entity.setPaymentState(Payment.State.PAID);

        Order order = mapper.toDomain(entity);

        assertThat(order.id()).isEqualTo(orderId);
        assertThat(order.productId()).isEqualTo(PRODUCT_ID);
        assertThat(order.quantity()).isEqualTo(QUANTITY);
        assertThat(order.money()).isEqualTo(new Money(new BigDecimal("10.00")));
        assertThat(order.state()).isEqualTo(Order.State.PAID);
        assertThat(order.payment().id()).isEqualTo(paymentId);
        assertThat(order.payment().state()).isEqualTo(Payment.State.PAID);
    }
}
