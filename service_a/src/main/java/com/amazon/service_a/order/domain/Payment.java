package com.amazon.service_a.order.domain;

import com.amazon.service_a.order.domain.exception.PaymentAlreadyPaidException;
import lombok.AccessLevel;
import lombok.Builder;

import java.util.UUID;

@Builder(access = AccessLevel.PRIVATE, setterPrefix = "with", toBuilder = true)
public record Payment(UUID id, State state) {

    public enum State {
        PENDING,
        PAID,
        FAILED
    }

    public Payment pay() {
        if (state == State.PAID) {
            throw new PaymentAlreadyPaidException(id);
        }

        return toBuilder().withState(State.PAID).build();
    }

    public Payment fail() {
        if (this.state == State.PAID) {
            throw new PaymentAlreadyPaidException(id);
        }

        if (this.state == State.FAILED) {
            return this;
        }

        return toBuilder().withState(State.FAILED).build();
    }
}
