package com.amazon.inventory_service.reservation.domain.exception;

import com.amazon.shared.core.domain.exception.DomainException;

public abstract class ReservationDomainException extends DomainException {
    protected ReservationDomainException(String message, String code) { super(message, code); }
}


