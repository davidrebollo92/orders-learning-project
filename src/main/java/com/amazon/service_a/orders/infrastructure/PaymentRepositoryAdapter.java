package com.amazon.service_a.orders.infrastructure;

import com.amazon.service_a.orders.domain.Payment;
import com.amazon.service_a.orders.domain.PaymentRepositoryPort;
import com.amazon.service_a.payments.infrastructure.JpaPaymentRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("ordersPaymentRepositoryAdapter")
public class PaymentRepositoryAdapter implements PaymentRepositoryPort {

    private final JpaPaymentRepository jpaPaymentRepository;

    public PaymentRepositoryAdapter(JpaPaymentRepository jpaPaymentRepository) {
        this.jpaPaymentRepository = jpaPaymentRepository;
    }

    @Override
    public Optional<Payment> findById(Long paymentId) {
        return jpaPaymentRepository.findById(paymentId)
                .map(entity -> new Payment(entity.getId(), entity.getState().name()));
    }
}
