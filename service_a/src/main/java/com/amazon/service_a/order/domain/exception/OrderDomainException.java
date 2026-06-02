package com.amazon.service_a.order.domain.exception;

import com.amazon.service_boot.core.domain.exception.DomainException;

public abstract class OrderDomainException extends DomainException {

    protected OrderDomainException(String message, String code) {
        super(message, code);
    }
}
