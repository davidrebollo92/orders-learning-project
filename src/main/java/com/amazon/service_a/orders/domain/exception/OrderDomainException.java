package com.amazon.service_a.orders.domain.exception;

// TODO en vez de extender de RuntimeException que extienda de uno custom que se encuentre en shared.domain.exception.DomainException
public abstract class OrderDomainException extends RuntimeException {

    protected OrderDomainException(String message) {
        super(message);
    }
}
