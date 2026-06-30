package com.amazon.inventory_service.reservation.domain.exception;

import java.util.UUID;

public class InvalidReservationStateException extends ReservationDomainException {
    public InvalidReservationStateException(UUID id) {
        super("Invalid reservation state for id: " + id, "INVALID_RESERVATION_STATE");
    }
}
