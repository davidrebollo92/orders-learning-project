package com.amazon.service_b.payment.domain;

import com.amazon.service_b.payment.domain.exception.InvalidPaymentStateException;
import com.amazon.service_b.payment.domain.exception.PaymentAlreadyPaidException;
import com.amazon.service_boot.core.domain.vo.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentTest {

    @Test
    void create_returnsPaymentInPendingState() {
        UUID id = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        Payment payment = Payment.create(id, orderId);

        assertThat(payment.id()).isEqualTo(id);
        assertThat(payment.orderId()).isEqualTo(orderId);
        assertThat(payment.state()).isEqualTo(Payment.State.PENDING);
        assertThat(payment.transaction()).isNull();
    }

    @Test
    void pay_returnsPaymentWithPaidStateAndTransaction() {
        Payment payment = Payment.create(UUID.randomUUID(), UUID.randomUUID());
        Transaction transaction = Transaction.create(new Money(new BigDecimal("50.00")));

        Payment paid = payment.pay(transaction);

        assertThat(paid.state()).isEqualTo(Payment.State.PAID);
        assertThat(paid.transaction()).isEqualTo(transaction);
        assertThat(paid.id()).isEqualTo(payment.id());
    }

    @Test
    void pay_throwsPaymentAlreadyPaidException_whenAlreadyPaid() {
        Transaction transaction = Transaction.create(new Money(new BigDecimal("50.00")));
        Payment payment = Payment.create(UUID.randomUUID(), UUID.randomUUID()).pay(transaction);

        assertThatThrownBy(() -> payment.pay(transaction))
                .isInstanceOf(PaymentAlreadyPaidException.class);
    }

    @Test
    void constructor_throwsInvalidPaymentStateException_whenPaidWithoutTransaction() {
        assertThatThrownBy(() -> new Payment(UUID.randomUUID(), UUID.randomUUID(), Payment.State.PAID, null))
                .isInstanceOf(InvalidPaymentStateException.class);
    }

    @Test
    void fail_returnsPaymentWithFailedState() {
        Payment payment = Payment.create(UUID.randomUUID(), UUID.randomUUID());

        Payment failed = payment.fail();

        assertThat(failed.state()).isEqualTo(Payment.State.FAILED);
        assertThat(failed.id()).isEqualTo(payment.id());
    }

    @Test
    void fail_returnsItself_whenAlreadyFailed() {
        Payment payment = Payment.create(UUID.randomUUID(), UUID.randomUUID()).fail();

        Payment result = payment.fail();

        assertThat(result).isSameAs(payment);
    }

    @Test
    void fail_throwsPaymentAlreadyPaidException_whenAlreadyPaid() {
        Transaction transaction = Transaction.create(new Money(new BigDecimal("50.00")));
        Payment payment = Payment.create(UUID.randomUUID(), UUID.randomUUID()).pay(transaction);

        assertThatThrownBy(payment::fail)
                .isInstanceOf(PaymentAlreadyPaidException.class);
    }
}
