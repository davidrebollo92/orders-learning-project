package com.amazon.inventory_service.reservation.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    Optional<Reservation> findByOrderId(UUID orderId);

    List<Reservation> findStalePending(Instant cutoff);

    void updateState(Reservation reservation);

}
