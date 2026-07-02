package com.amazon.order_service.order.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentTest {

    @Test
    void pending_returnsPendingPaymentWithoutId() {
        Payment payment = Payment.pending();

        assertThat(payment.id()).isNull();
        assertThat(payment.state()).isEqualTo(Payment.State.PENDING);
    }

    @Test
    void paid_returnsPaidPaymentWithId() {
        UUID id = UUID.randomUUID();

        Payment payment = Payment.paid(id);

        assertThat(payment.id()).isEqualTo(id);
        assertThat(payment.state()).isEqualTo(Payment.State.PAID);
    }

    @Test
    void failed_returnsFailedPaymentWithId() {
        UUID id = UUID.randomUUID();

        Payment payment = Payment.failed(id);

        assertThat(payment.id()).isEqualTo(id);
        assertThat(payment.state()).isEqualTo(Payment.State.FAILED);
    }
}
