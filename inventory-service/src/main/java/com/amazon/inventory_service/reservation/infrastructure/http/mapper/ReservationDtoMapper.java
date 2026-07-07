package com.amazon.inventory_service.reservation.infrastructure.http.mapper;

import com.amazon.inventory_service.reservation.domain.Reservation;
import com.amazon.inventory_service.reservation.infrastructure.http.dto.CreateReservationRequest;
import com.amazon.inventory_service.reservation.infrastructure.http.dto.ReservationResponse;
import org.springframework.stereotype.Component;

@Component
public class ReservationDtoMapper {

    public Reservation toDomain(CreateReservationRequest request) {
        return Reservation.create(request.getOrderId(), request.getProductId(), request.getQuantity());
    }

    public ReservationResponse toResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.id(),
                reservation.orderId(),
                reservation.productId(),
                reservation.quantity(),
                ReservationResponse.StateEnum.fromValue(reservation.state().name())
        );
    }
}
