package com.amazon.service_a.shared.infrastructure;

import com.amazon.service_a.orders.domain.exception.OrderDomainException;
import com.amazon.service_a.orders.domain.exception.OrderNotFoundException;
import com.amazon.service_a.payments.domain.exception.PaymentDomainException;
import com.amazon.service_a.payments.domain.exception.PaymentNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
// TODO en shared debe estar lo generico. Si haces uso de OrderNotFoundException y PaymentNotFoundException deberian ir en sus correspondientes paquetes.
public class GlobalExceptionHandler {

    @ExceptionHandler({OrderNotFoundException.class, PaymentNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNotFound(RuntimeException ex) {
        return ApiResponse.error(ex.getMessage());
    }

    @ExceptionHandler({OrderDomainException.class, PaymentDomainException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleDomainException(RuntimeException ex) {
        return ApiResponse.error(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ApiResponse.error(message);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleUnexpected(Exception ex) {
        return ApiResponse.error("An unexpected error occurred");
    }
}
