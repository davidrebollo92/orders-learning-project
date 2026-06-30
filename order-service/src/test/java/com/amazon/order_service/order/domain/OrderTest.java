package com.amazon.order_service.order.domain;

import com.amazon.order_service.order.domain.exception.InvalidOrderAmountException;
import com.amazon.order_service.order.domain.exception.PaymentAlreadyPaidException;
import com.amazon.shared.core.domain.vo.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final int QUANTITY = 2;
    private static final Money MONEY = new Money(new BigDecimal("10.00"));

    @Test
    void create_returnsOrderWithProductIdQuantityAndAmount() {
        Order order = Order.create(PRODUCT_ID, QUANTITY, MONEY);

        assertThat(order.id()).isNotNull();
        assertThat(order.productId()).isEqualTo(PRODUCT_ID);
        assertThat(order.quantity()).isEqualTo(QUANTITY);
        assertThat(order.money()).isEqualTo(MONEY);
        assertThat(order.state()).isEqualTo(Order.State.CREATED);
        assertThat(order.payment()).isNull();
    }

    @Test
    void create_throwsInvalidOrderAmountException_whenAmountIsBelowMinimum() {
        Money money = new Money(BigDecimal.ZERO);

        assertThatThrownBy(() -> Order.create(PRODUCT_ID, QUANTITY, money))
                .isInstanceOf(InvalidOrderAmountException.class);
    }

    @Test
    void addPayment_returnsOrderWithPendingPayment() {
        Order order = Order.create(PRODUCT_ID, QUANTITY, MONEY);

        Order withPayment = order.addPayment();

        assertThat(withPayment.payment()).isNotNull();
        assertThat(withPayment.payment().id()).isNotNull();
        assertThat(withPayment.payment().state()).isEqualTo(Payment.State.PENDING);
    }

    @Test
    void completePayment_returnsOrderWithPaidPayment() {
        Order order = Order.create(PRODUCT_ID, QUANTITY, MONEY).addPayment();

        Order completed = order.completePayment();

        assertThat(completed.payment().state()).isEqualTo(Payment.State.PAID);
        assertThat(completed.payment().id()).isEqualTo(order.payment().id());
    }

    @Test
    void completePayment_throwsPaymentAlreadyPaidException_whenPaymentAlreadyPaid() {
        Order order = Order.create(PRODUCT_ID, QUANTITY, MONEY).addPayment().completePayment();

        assertThatThrownBy(order::completePayment)
                .isInstanceOf(PaymentAlreadyPaidException.class);
    }

    @Test
    void cancel_returnsOrderWithCancelledStateAndFailedPayment() {
        Order order = Order.create(PRODUCT_ID, QUANTITY, MONEY).addPayment();

        Order cancelled = order.cancel();

        assertThat(cancelled.state()).isEqualTo(Order.State.CANCELLED);
        assertThat(cancelled.payment().state()).isEqualTo(Payment.State.FAILED);
        assertThat(cancelled.payment().id()).isEqualTo(order.payment().id());
    }

    @Test
    void cancel_isIdempotent_whenPaymentAlreadyFailed() {
        Order order = Order.create(PRODUCT_ID, QUANTITY, MONEY).addPayment().cancel();

        Order cancelledAgain = order.cancel();

        assertThat(cancelledAgain.state()).isEqualTo(Order.State.CANCELLED);
        assertThat(cancelledAgain.payment().state()).isEqualTo(Payment.State.FAILED);
    }
}
