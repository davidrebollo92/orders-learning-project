package com.amazon.inventory_service.reservation.infrastructure.http.mapper;

import com.amazon.inventory_service.reservation.domain.Reservation;
import com.amazon.inventory_service.reservation.infrastructure.http.dto.CreateReservationRequest;
import com.amazon.inventory_service.reservation.infrastructure.http.dto.ReservationResponse;
import org.springframework.stereotype.Component;

@Component
public class ReservationDtoMapper {

    public Reservation toDomain(CreateReservationRequest request) {
        return Reservation.create(request.orderId(), request.productId(), request.quantity());
    }

    public ReservationResponse toResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.id(),
                reservation.orderId(),
                reservation.productId(),
                reservation.quantity(),
                reservation.state().name()
        );
    }
}
