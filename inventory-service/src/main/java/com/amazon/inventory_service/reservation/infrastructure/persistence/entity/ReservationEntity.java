package com.amazon.inventory_service.reservation.infrastructure.persistence.entity;

import com.amazon.inventory_service.reservation.domain.Reservation;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reservations")
@Getter
@Setter
public class ReservationEntity {

    @Id
    private UUID id;

    private UUID orderId;

    private UUID productId;

    private int quantity;

    @Enumerated(EnumType.STRING)
    private Reservation.State state;

    private Instant createdAt;
}
