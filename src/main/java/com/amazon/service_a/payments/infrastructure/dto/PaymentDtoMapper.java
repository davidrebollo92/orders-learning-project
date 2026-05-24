package com.amazon.service_a.payments.infrastructure.dto;

import com.amazon.service_a.payments.domain.Payment;

public class PaymentDtoMapper {

    public static PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(payment.getId(), payment.getState().name());
    }
}
