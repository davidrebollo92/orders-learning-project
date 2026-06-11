package com.amazon.service_b.payment.domain;

import com.amazon.service_b.payment.domain.exception.PaymentAlreadyPaidException;
import com.amazon.service_boot.core.domain.vo.Money;
import lombok.AccessLevel;
import lombok.Builder;

import java.util.UUID;

@Builder(access = AccessLevel.PRIVATE, setterPrefix = "with", toBuilder = true)
public record Payment(UUID id, UUID orderId, State state, Transaction transaction) {

    public enum State {
        PENDING,
        PAID,
        FAILED
    }


    public Payment pay(UUID transactionId) {
        if (this.state == State.PAID) {
            throw new PaymentAlreadyPaidException(this.id);
        }

        return toBuilder().withState(State.PAID).withTransaction(transaction.complete(transactionId)).build();
    }

    public Payment fail() {
        if (this.state == State.PAID) {
            throw new PaymentAlreadyPaidException(this.id);
        }

        if (this.state == State.FAILED) {
            return this;
        }

        return toBuilder().withState(State.FAILED).build();
    }

    public Money getAmount() {
        return transaction.amount();
    }

    public static Payment create(UUID id, UUID orderId, Money money) {
        final Transaction transaction = Transaction.create(money);

        return new Payment(id, orderId, State.PENDING, transaction);
    }
}

