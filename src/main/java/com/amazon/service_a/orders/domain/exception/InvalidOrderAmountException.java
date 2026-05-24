package com.amazon.service_a.orders.domain.exception;

public class InvalidOrderAmountException extends OrderDomainException {

    public InvalidOrderAmountException() {
        super("Order amount must be greater than zero");
    }
}
