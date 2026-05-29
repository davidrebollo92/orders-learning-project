package com.amazon.service_a.order.domain;

import java.util.UUID;

public interface PaymentRepository {
    void completePayment(UUID paymentId);
}
