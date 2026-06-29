package com.amazon.inventory_service.reservation.domain;

import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    Optional<Reservation> findByOrderId(UUID orderId);

    void updateState(Reservation reservation);

}
