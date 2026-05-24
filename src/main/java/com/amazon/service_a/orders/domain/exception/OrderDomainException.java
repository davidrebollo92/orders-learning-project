package com.amazon.service_a.orders.domain.exception;

public abstract class OrderDomainException extends RuntimeException {

    protected OrderDomainException(String message) {
        super(message);
    }
}
