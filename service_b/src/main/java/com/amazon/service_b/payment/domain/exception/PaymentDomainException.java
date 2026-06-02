package com.amazon.service_b.payment.domain.exception;

import com.amazon.service_b.shared.domain.exception.DomainException;

public abstract class PaymentDomainException extends DomainException {

    protected PaymentDomainException(String message, String code) {
        super(message, code);
    }
}
