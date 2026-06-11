package com.amazon.service_a.order.domain;

import com.amazon.service_a.order.domain.exception.InvalidOrderAmountException;
import com.amazon.service_a.order.domain.exception.PaymentAlreadyPaidException;
import com.amazon.service_boot.core.domain.vo.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    @Test
    void create_returnsOrderWithGivenNameAndAmount() {
        Money money = new Money(new BigDecimal("10.00"));

        Order order = Order.create("laptop", money);

        assertThat(order.id()).isNotNull();
        assertThat(order.name()).isEqualTo("laptop");
        assertThat(order.money()).isEqualTo(money);
        assertThat(order.state()).isEqualTo(Order.State.CREATED);
        assertThat(order.payment()).isNull();
    }

    @Test
    void create_throwsInvalidOrderAmountException_whenAmountIsBelowMinimum() {
        Money money = new Money(BigDecimal.ZERO);

        assertThatThrownBy(() -> Order.create("laptop", money))
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

    @Test
    void completePayment_throwsPaymentAlreadyPaidException_whenPaymentAlreadyPaid() {
        Order order = Order.create("laptop", new Money(new BigDecimal("10.00"))).addPayment().completePayment();

        assertThatThrownBy(order::completePayment)
                .isInstanceOf(PaymentAlreadyPaidException.class);
    }

    @Test
    void cancel_returnsOrderWithCancelledStateAndFailedPayment() {
        Order order = Order.create("laptop", new Money(new BigDecimal("10.00"))).addPayment();

        Order cancelled = order.cancel();

        assertThat(cancelled.state()).isEqualTo(Order.State.CANCELLED);
        assertThat(cancelled.payment().state()).isEqualTo(Payment.State.FAILED);
        assertThat(cancelled.payment().id()).isEqualTo(order.payment().id());
    }

    @Test
    void cancel_isIdempotent_whenPaymentAlreadyFailed() {
        Order order = Order.create("laptop", new Money(new BigDecimal("10.00"))).addPayment().cancel();

        Order cancelledAgain = order.cancel();

        assertThat(cancelledAgain.state()).isEqualTo(Order.State.CANCELLED);
        assertThat(cancelledAgain.payment().state()).isEqualTo(Payment.State.FAILED);
    }
}
