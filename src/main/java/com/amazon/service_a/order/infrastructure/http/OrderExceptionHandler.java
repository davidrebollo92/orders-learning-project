package com.amazon.service_a.order.infrastructure.http;

import com.amazon.service_a.order.domain.exception.OrderDomainException;
import com.amazon.service_a.order.domain.exception.OrderNotFoundException;
import com.amazon.service_a.shared.infrastructure.ErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class OrderExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorDto> handleNotFound(OrderNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorDto(ex.getMessage(), ex.getCode()));
    }

    @ExceptionHandler(OrderDomainException.class)
    public ResponseEntity<ErrorDto> handleDomainException(OrderDomainException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDto(ex.getMessage(), ex.getCode()));
    }
}
