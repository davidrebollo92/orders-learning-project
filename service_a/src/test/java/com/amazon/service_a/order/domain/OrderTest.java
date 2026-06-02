package com.amazon.service_a.order.domain;

import com.amazon.service_a.order.domain.exception.InvalidOrderAmountException;
import com.amazon.service_boot.core.domain.vo.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    @Test
    void create_returnsOrderWithGivenNameAndAmount() {
        Money amount = new Money(new BigDecimal("10.00"));

        Order order = Order.create("laptop", amount);

        assertThat(order.id()).isNotNull();
        assertThat(order.name()).isEqualTo("laptop");
        assertThat(order.amount()).isEqualTo(amount);
        assertThat(order.payment()).isNull();
    }

    @Test
    void create_throwsInvalidOrderAmountException_whenAmountIsBelowMinimum() {
        Money amount = new Money(BigDecimal.ZERO);

        assertThatThrownBy(() -> Order.create("laptop", amount))
                .isInstanceOf(InvalidOrderAmountException.class);
    }

    @Test
    void addPayment_returnsOrderWithPendingPayment() {
        Order order = Order.create("laptop", new Money(new BigDecimal("10.00")));

        Order withPayment = order.addPayment();

        assertThat(withPayment.payment()).isNotNull();
        assertThat(withPayment.payment().id()).isNotNull();
        assertThat(withPayment.payment().state()).isEqualTo(Payment.State.PENDING);
    }

    @Test
    void completePayment_returnsOrderWithPaidPayment() {
        Order order = Order.create("laptop", new Money(new BigDecimal("10.00"))).addPayment();

        Order completed = order.completePayment();

        assertThat(completed.payment().state()).isEqualTo(Payment.State.PAID);
        assertThat(completed.payment().id()).isEqualTo(order.payment().id());
    }
}
