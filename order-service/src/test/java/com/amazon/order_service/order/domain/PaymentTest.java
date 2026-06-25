package com.amazon.order_service.order.domain;

import com.amazon.order_service.order.domain.exception.PaymentAlreadyPaidException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentTest {

    @Test
    void pay_returnsPaymentWithPaidState() {
        Payment payment = new Payment(UUID.randomUUID(), Payment.State.PENDING);

        Payment paid = payment.pay();

        assertThat(paid.state()).isEqualTo(Payment.State.PAID);
        assertThat(paid.id()).isEqualTo(payment.id());
    }

    @Test
    void pay_throwsPaymentAlreadyPaidException_whenAlreadyPaid() {
        Payment payment = new Payment(UUID.randomUUID(), Payment.State.PAID);

        assertThatThrownBy(payment::pay)
                .isInstanceOf(PaymentAlreadyPaidException.class);
    }

    @Test
    void fail_returnsPaymentWithFailedState() {
        Payment payment = new Payment(UUID.randomUUID(), Payment.State.PENDING);

        Payment failed = payment.fail();

        assertThat(failed.state()).isEqualTo(Payment.State.FAILED);
        assertThat(failed.id()).isEqualTo(payment.id());
    }

    @Test
    void fail_returnsItself_whenAlreadyFailed() {
        Payment payment = new Payment(UUID.randomUUID(), Payment.State.FAILED);

        Payment result = payment.fail();

        assertThat(result).isSameAs(payment);
    }

    @Test
    void fail_throwsPaymentAlreadyPaidException_whenAlreadyPaid() {
        Payment payment = new Payment(UUID.randomUUID(), Payment.State.PAID);

        assertThatThrownBy(payment::fail)
                .isInstanceOf(PaymentAlreadyPaidException.class);
    }
}
