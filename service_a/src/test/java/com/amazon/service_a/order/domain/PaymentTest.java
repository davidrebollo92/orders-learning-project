package com.amazon.service_a.order.domain;

import com.amazon.service_a.order.domain.exception.PaymentAlreadyPaidException;
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
}
