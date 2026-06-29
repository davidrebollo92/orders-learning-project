package com.amazon.inventory_service.reservation.domain.exception;

import java.util.UUID;

public class ReservationAlreadyExistsException extends ReservationDomainException {
    public ReservationAlreadyExistsException(UUID orderId) {
        super("Reservation already exists for order: " + orderId, "RESERVATION_ALREADY_EXISTS");
    }
}
