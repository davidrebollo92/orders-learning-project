package com.amazon.service_a.payment.domain;

import com.amazon.service_a.payment.domain.exception.InvalidPaymentStateException;
import com.amazon.service_a.payment.domain.exception.PaymentAlreadyPaidException;

import java.util.UUID;

public record Payment(UUID id, State state, Transaction transaction) {

    public enum State {
        PENDING,
        PAID
    }

    public Payment {
        if (state == State.PAID && transaction == null) {
            throw new InvalidPaymentStateException();
        }
    }

    public static Payment create(UUID id, State state) {
        return new Payment(id, state, null);
    }

    public Payment pay(Transaction transaction) {
        if (this.state == State.PAID) {
            throw new PaymentAlreadyPaidException(this.id);
        }

        return new Payment(this.id, State.PAID, transaction);
    }
}

