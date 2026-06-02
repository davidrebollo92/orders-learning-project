package com.amazon.service_b.payment.domain;

import com.amazon.service_b.payment.domain.exception.InvalidPaymentStateException;
import com.amazon.service_b.payment.domain.exception.PaymentAlreadyPaidException;
import lombok.AccessLevel;
import lombok.Builder;

import java.util.UUID;

@Builder(access = AccessLevel.PRIVATE, setterPrefix = "with", toBuilder = true)
public record Payment(UUID id, UUID orderId, State state, Transaction transaction) {

    public enum State {
        PENDING,
        PAID
    }

    public Payment {
        if (state == State.PAID && transaction == null) {
            throw new InvalidPaymentStateException();
        }
    }

    public static Payment create(UUID id, UUID orderId) {
        return new Payment(id, orderId, State.PENDING, null);
    }

    public Payment pay(Transaction transaction) {
        if (this.state == State.PAID) {
            throw new PaymentAlreadyPaidException(this.id);
        }

        return toBuilder().withState(State.PAID).withTransaction(transaction).build();
    }
}

