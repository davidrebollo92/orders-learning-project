package com.amazon.order_service.order.infrastructure.http.mapper;

import com.amazon.order_service.order.domain.Order;
import com.amazon.order_service.order.domain.Payment;
import com.amazon.order_service.order.infrastructure.http.dto.CreateOrderRequest;
import com.amazon.order_service.order.infrastructure.http.dto.OrderResponse;
import com.amazon.shared.core.domain.vo.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderDtoMapperTest {

    private final OrderDtoMapper mapper = new OrderDtoMapper();

    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final int QUANTITY = 2;

    @Test
    void toDomain_returnsOrderWithProductIdQuantityAndAmount() {
        CreateOrderRequest request = new CreateOrderRequest(PRODUCT_ID, QUANTITY, new BigDecimal("10.00"));

        Order order = mapper.toDomain(request);

        assertThat(order.productId()).isEqualTo(PRODUCT_ID);
        assertThat(order.quantity()).isEqualTo(QUANTITY);
        assertThat(order.money()).isEqualTo(new Money(new BigDecimal("10.00")));
        assertThat(order.id()).isNotNull();
        assertThat(order.payment()).isNull();
    }

    @Test
    void toResponse_returnsOrderResponseWithAllFields() {
        Order order = Order.create(PRODUCT_ID, QUANTITY, new Money(new BigDecimal("10.00"))).addPayment();

        OrderResponse response = mapper.toResponse(order);

        assertThat(response.getId()).isEqualTo(order.id());
        assertThat(response.getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(response.getQuantity()).isEqualTo(QUANTITY);
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(response.getPayment().getId()).isEqualTo(order.payment().id());
        assertThat(response.getPayment().getState().name()).isEqualTo(Payment.State.PENDING.name());
    }

    @Test
    void toResponse_reflectsPaidState_whenPaymentIsCompleted() {
        Order order = Order.create(PRODUCT_ID, QUANTITY, new Money(new BigDecimal("10.00"))).addPayment().completePayment();

        OrderResponse response = mapper.toResponse(order);

        assertThat(response.getPayment().getState().name()).isEqualTo(Payment.State.PAID.name());
    }
}
