package com.amazon.inventory_service.reservation.domain.exception;

import java.util.UUID;

public class ReservationNotFoundException extends ReservationDomainException {
    public ReservationNotFoundException(UUID id) {
        super("Reservation not found with order id: " + id, "RESERVATION_NOT_FOUND");
    }
}
