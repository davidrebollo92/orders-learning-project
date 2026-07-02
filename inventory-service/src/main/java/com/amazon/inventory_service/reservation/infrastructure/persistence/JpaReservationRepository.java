package com.amazon.inventory_service.reservation.infrastructure.persistence;

import com.amazon.inventory_service.reservation.domain.Reservation;
import com.amazon.inventory_service.reservation.infrastructure.persistence.entity.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaReservationRepository extends JpaRepository<ReservationEntity, UUID> {

    Optional<ReservationEntity> findByOrderId(UUID orderId);

    List<ReservationEntity> findByStateAndCreatedAtBefore(Reservation.State state, Instant cutoff);

    @Modifying
    @Query("UPDATE ReservationEntity r SET r.state = :state WHERE r.id = :id")
    void updateState(@Param("id") UUID id, @Param("state") Reservation.State state);
}
