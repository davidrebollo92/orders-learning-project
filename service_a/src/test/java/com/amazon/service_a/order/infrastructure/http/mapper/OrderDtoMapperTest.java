package com.amazon.service_a.order.infrastructure.http.mapper;

import com.amazon.service_a.order.domain.Order;
import com.amazon.service_a.order.domain.Payment;
import com.amazon.service_a.order.infrastructure.http.dto.CreateOrderRequest;
import com.amazon.service_a.order.infrastructure.http.dto.OrderResponse;
import com.amazon.service_boot.core.domain.vo.Money;
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
        assertThat(order.amount()).isEqualTo(new Money(new BigDecimal("10.00")));
        assertThat(order.id()).isNotNull();
        assertThat(order.payment()).isNull();
    }

    @Test
    void toResponse_returnsOrderResponseWithAllFields() {
        Order order = Order.create("laptop", new Money(new BigDecimal("10.00"))).addPayment();

        OrderResponse response = mapper.toResponse(order);

        assertThat(response.id()).isEqualTo(order.id());
        assertThat(response.name()).isEqualTo("laptop");
        assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(response.payment().id()).isEqualTo(order.payment().id());
        assertThat(response.payment().state()).isEqualTo(Payment.State.PENDING.name());
    }

    @Test
    void toResponse_reflectsPaidState_whenPaymentIsCompleted() {
        Order order = Order.create("laptop", new Money(new BigDecimal("10.00"))).addPayment().completePayment();

        OrderResponse response = mapper.toResponse(order);

        assertThat(response.payment().state()).isEqualTo(Payment.State.PAID.name());
    }
}
