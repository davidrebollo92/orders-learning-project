package com.amazon.service_b.payment.domain;

import com.amazon.service_b.payment.domain.exception.PaymentAlreadyPaidException;
import com.amazon.service_boot.core.domain.vo.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentTest {

    private final UUID id = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();
    private final Money money = new Money(new BigDecimal("50.00"));

    @Test
    void create_returnsPaymentInPendingState() {
        Payment payment = Payment.create(id, orderId, money);

        assertThat(payment.id()).isEqualTo(id);
        assertThat(payment.orderId()).isEqualTo(orderId);
        assertThat(payment.state()).isEqualTo(Payment.State.PENDING);
        assertThat(payment.transaction().id()).isNull();
        assertThat(payment.transaction().money()).isEqualTo(money);
    }

    @Test
    void pay_returnsPaymentWithPaidStateAndTransaction() {
        Payment payment = Payment.create(UUID.randomUUID(), UUID.randomUUID(), money);
        UUID transactionId = UUID.randomUUID();

        Payment paid = payment.pay(transactionId);

        assertThat(paid.state()).isEqualTo(Payment.State.PAID);
        assertThat(paid.transaction().id()).isEqualTo(transactionId);
        assertThat(paid.transaction().money()).isEqualTo(money);
        assertThat(paid.id()).isEqualTo(payment.id());
    }

    @Test
    void pay_throwsPaymentAlreadyPaidException_whenAlreadyPaid() {
        Payment payment = Payment.create(UUID.randomUUID(), UUID.randomUUID(), money).pay(UUID.randomUUID());

        assertThatThrownBy(() -> payment.pay(UUID.randomUUID()))
                .isInstanceOf(PaymentAlreadyPaidException.class);
    }

    @Test
    void fail_returnsPaymentWithFailedState() {
        Payment payment = Payment.create(UUID.randomUUID(), UUID.randomUUID(), money);

        Payment failed = payment.fail();

        assertThat(failed.state()).isEqualTo(Payment.State.FAILED);
        assertThat(failed.id()).isEqualTo(payment.id());
    }

    @Test
    void fail_returnsItself_whenAlreadyFailed() {
        Payment payment = Payment.create(UUID.randomUUID(), UUID.randomUUID(), money).fail();

        Payment result = payment.fail();

        assertThat(result).isSameAs(payment);
    }

    @Test
    void fail_throwsPaymentAlreadyPaidException_whenAlreadyPaid() {
        Payment payment = Payment.create(UUID.randomUUID(), UUID.randomUUID(), money).pay(UUID.randomUUID());

        assertThatThrownBy(payment::fail)
                .isInstanceOf(PaymentAlreadyPaidException.class);
    }
}
