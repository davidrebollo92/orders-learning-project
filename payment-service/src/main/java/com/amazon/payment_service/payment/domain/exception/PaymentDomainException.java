package com.amazon.payment_service.payment.domain.exception;

import com.amazon.shared.core.domain.exception.DomainException;

public abstract class PaymentDomainException extends DomainException {

    protected PaymentDomainException(String message, String code) {
        super(message, code);
    }
}
