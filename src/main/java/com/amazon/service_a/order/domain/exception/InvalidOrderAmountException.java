package com.amazon.service_a.order.domain.exception;

public class InvalidOrderAmountException extends OrderDomainException {

    public InvalidOrderAmountException() {
        super("Order amount must be greater than zero", "INVALID_ORDER_AMOUNT");
    }
}
