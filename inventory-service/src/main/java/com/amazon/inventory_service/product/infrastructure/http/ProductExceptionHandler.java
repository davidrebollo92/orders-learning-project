package com.amazon.inventory_service.product.infrastructure.http;

import com.amazon.inventory_service.product.domain.exception.ProductDomainException;
import com.amazon.inventory_service.product.domain.exception.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.amazon.shared.core.infrastructure.http.ErrorDto;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ProductExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorDto> handleNotFoundException(ProductNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorDto(ex.getMessage(), ex.getCode()));
    }

    @ExceptionHandler(ProductDomainException.class)
    public ResponseEntity<ErrorDto> handleDomainException(ProductDomainException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDto(ex.getMessage(), ex.getCode()));
    }
}
