package com.amazon.order_service.order.infrastructure.http.mapper;

import com.amazon.order_service.order.domain.Order;
import com.amazon.order_service.order.domain.Payment;
import com.amazon.order_service.order.infrastructure.http.dto.CreateOrderRequest;
import com.amazon.order_service.order.infrastructure.http.dto.OrderResponse;
import com.amazon.shared.core.domain.vo.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class OrderDtoMapperTest {

    private final OrderDtoMapper mapper = new OrderDtoMapper();

    @Test
    void toDomain_returnsOrderWithNameAndAmount() {
        CreateOrderRequest request = new CreateOrderRequest("laptop", new BigDecimal("10.00"));

        Order order = mapper.toDomain(request);

        assertThat(order.name()).isEqualTo("laptop");
        assertThat(order.money()).isEqualTo(new Money(new BigDecimal("10.00")));
        assertThat(order.id()).isNotNull();
        assertThat(order.payment()).isNull();
    }

    @Test
    void toResponse_returnsOrderResponseWithAllFields() {
        Order order = Order.create("laptop", new Money(new BigDecimal("10.00"))).addPayment();

        OrderResponse response = mapper.toResponse(order);

        assertThat(response.getId()).isEqualTo(order.id());
        assertThat(response.getName()).isEqualTo("laptop");
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(response.getPayment().getId()).isEqualTo(order.payment().id());
        assertThat(response.getPayment().getState().name()).isEqualTo(Payment.State.PENDING.name());
    }

    @Test
    void toResponse_reflectsPaidState_whenPaymentIsCompleted() {
        Order order = Order.create("laptop", new Money(new BigDecimal("10.00"))).addPayment().completePayment();

        OrderResponse response = mapper.toResponse(order);

        assertThat(response.getPayment().getState().name()).isEqualTo(Payment.State.PAID.name());
    }
}
