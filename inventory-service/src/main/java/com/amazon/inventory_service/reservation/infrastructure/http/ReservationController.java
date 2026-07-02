package com.amazon.inventory_service.reservation.infrastructure.http;

import com.amazon.inventory_service.reservation.aplication.StockReserver;
import com.amazon.inventory_service.reservation.domain.Reservation;
import com.amazon.inventory_service.reservation.infrastructure.http.dto.CreateReservationRequest;
import com.amazon.inventory_service.reservation.infrastructure.http.dto.ReservationResponse;
import com.amazon.inventory_service.reservation.infrastructure.http.mapper.ReservationDtoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReservationController {

    private final StockReserver stockReserver;
    private final ReservationDtoMapper reservationDtoMapper;

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody CreateReservationRequest request) {
        Reservation reservation = reservationDtoMapper.toDomain(request);

        stockReserver.reserve(reservation);

        return ResponseEntity.status(HttpStatus.CREATED).body(reservationDtoMapper.toResponse(reservation));
    }
}
