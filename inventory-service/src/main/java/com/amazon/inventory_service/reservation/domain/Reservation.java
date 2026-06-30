package com.amazon.inventory_service.reservation.domain;

import com.amazon.inventory_service.reservation.domain.exception.InvalidReservationStateException;
import lombok.AccessLevel;
import lombok.Builder;

import java.util.UUID;

@Builder(access = AccessLevel.PRIVATE, setterPrefix = "with", toBuilder = true)
public record Reservation(UUID id, UUID orderId, UUID productId, int quantity, State state) {

    public enum State {
        PENDING,
        CONFIRMED,
        RELEASED
    }

    public static Reservation create(UUID orderId, UUID productId, int quantity) {
        return new Reservation(UUID.randomUUID(), orderId, productId, quantity, State.PENDING);
    }

    public Reservation confirm() {
        if (state != State.PENDING) throw new InvalidReservationStateException(id);

        return toBuilder().withState(State.CONFIRMED).build();
    }

    public Reservation release() {
        if (state != State.PENDING) throw new InvalidReservationStateException(id);

        return toBuilder().withState(State.RELEASED).build();
    }
}
