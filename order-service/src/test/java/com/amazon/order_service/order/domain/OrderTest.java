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
    void create_returnsOrderWithPendingPayment() {
        Order order = Order.create(PRODUCT_ID, QUANTITY, MONEY);

        assertThat(order.id()).isNotNull();
        assertThat(order.productId()).isEqualTo(PRODUCT_ID);
        assertThat(order.quantity()).isEqualTo(QUANTITY);
        assertThat(order.money()).isEqualTo(new Money(new BigDecimal("20.00")));
        assertThat(order.state()).isEqualTo(Order.State.CREATED);
        assertThat(order.payment().state()).isEqualTo(Payment.State.PENDING);
        assertThat(order.payment().id()).isNull();
    }

    @Test
    void create_throwsInvalidOrderAmountException_whenAmountIsBelowMinimum() {
        Money money = new Money(BigDecimal.ZERO);

        assertThatThrownBy(() -> Order.create(PRODUCT_ID, QUANTITY, money))
                .isInstanceOf(InvalidOrderAmountException.class);
    }

    @Test
    void markPaid_returnsOrderPaidWithPaymentReference() {
        UUID paymentId = UUID.randomUUID();

        Order paid = Order.create(PRODUCT_ID, QUANTITY, MONEY).markPaid(paymentId);

        assertThat(paid.state()).isEqualTo(Order.State.PAID);
        assertThat(paid.payment().state()).isEqualTo(Payment.State.PAID);
        assertThat(paid.payment().id()).isEqualTo(paymentId);
    }

    @Test
    void markPaid_throwsPaymentAlreadyPaidException_whenAlreadyPaid() {
        Order paid = Order.create(PRODUCT_ID, QUANTITY, MONEY).markPaid(UUID.randomUUID());

        assertThatThrownBy(() -> paid.markPaid(UUID.randomUUID()))
                .isInstanceOf(PaymentAlreadyPaidException.class);
    }

    @Test
    void cancel_returnsOrderCancelledWithFailedPayment() {
        UUID paymentId = UUID.randomUUID();

        Order cancelled = Order.create(PRODUCT_ID, QUANTITY, MONEY).cancel(paymentId);

        assertThat(cancelled.state()).isEqualTo(Order.State.CANCELLED);
        assertThat(cancelled.payment().state()).isEqualTo(Payment.State.FAILED);
        assertThat(cancelled.payment().id()).isEqualTo(paymentId);
    }

    @Test
    void cancel_isIdempotent_whenAlreadyCancelled() {
        Order cancelled = Order.create(PRODUCT_ID, QUANTITY, MONEY).cancel(UUID.randomUUID());

        Order cancelledAgain = cancelled.cancel(UUID.randomUUID());

        assertThat(cancelledAgain).isSameAs(cancelled);
        assertThat(cancelledAgain.state()).isEqualTo(Order.State.CANCELLED);
        assertThat(cancelledAgain.payment().state()).isEqualTo(Payment.State.FAILED);
    }
}
