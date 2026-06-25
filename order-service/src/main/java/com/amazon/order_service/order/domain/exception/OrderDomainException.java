package com.amazon.order_service.order.domain.exception;

import com.amazon.shared.core.domain.exception.DomainException;

public abstract class OrderDomainException extends DomainException {

    protected OrderDomainException(String message, String code) {
        super(message, code);
    }
}
