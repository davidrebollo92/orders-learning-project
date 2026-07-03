package com.amazon.order_service.order.infrastructure.http;

import com.amazon.order_service.order.domain.exception.InsufficientStockException;
import com.amazon.order_service.order.domain.exception.InventoryUnavailableException;
import com.amazon.order_service.order.domain.exception.OrderDomainException;
import com.amazon.order_service.order.domain.exception.OrderNotFoundException;
import com.amazon.shared.core.infrastructure.http.ErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class OrderExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorDto> handleNotFoundException(OrderNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorDto(ex.getMessage(), ex.getCode()));
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorDto> handleInsufficientStock(InsufficientStockException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorDto(ex.getMessage(), ex.getCode()));
    }

    @ExceptionHandler(InventoryUnavailableException.class)
    public ResponseEntity<ErrorDto> handleInventoryUnavailable(InventoryUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorDto(ex.getMessage(), ex.getCode()));
    }

    @ExceptionHandler(OrderDomainException.class)
    public ResponseEntity<ErrorDto> handleDomainException(OrderDomainException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDto(ex.getMessage(), ex.getCode()));
    }
}
