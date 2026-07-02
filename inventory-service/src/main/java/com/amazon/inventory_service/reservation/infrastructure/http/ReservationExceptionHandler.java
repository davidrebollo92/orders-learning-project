package com.amazon.inventory_service.reservation.infrastructure.http;

import com.amazon.inventory_service.reservation.domain.exception.ReservationAlreadyExistsException;
import com.amazon.shared.core.infrastructure.http.ErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ReservationExceptionHandler {

    @ExceptionHandler(ReservationAlreadyExistsException.class)
    public ResponseEntity<ErrorDto> handleAlreadyExists(ReservationAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorDto(ex.getMessage(), ex.getCode()));
    }
}
