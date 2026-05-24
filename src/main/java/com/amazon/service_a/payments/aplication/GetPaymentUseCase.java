package com.amazon.service_a.payments.aplication;

import com.amazon.service_a.payments.domain.Payment;

public interface GetPaymentUseCase {
    Payment getPayment(Long id);
}
