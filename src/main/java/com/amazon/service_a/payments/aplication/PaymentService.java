package com.amazon.service_a.payments.aplication;

import com.amazon.service_a.payments.domain.Payment;
import com.amazon.service_a.payments.domain.PaymentRepositoryPort;
import com.amazon.service_a.payments.domain.exception.PaymentNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PaymentService implements GetPaymentUseCase {

    private final PaymentRepositoryPort repository;

    public PaymentService(PaymentRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Payment getPayment(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
    }
}
