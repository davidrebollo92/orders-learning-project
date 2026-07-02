package com.amazon.payment_service.payment.aplication;

import com.amazon.payment_service.payment.domain.Payment;
import com.amazon.payment_service.payment.domain.PaymentEventPublisher;
import com.amazon.payment_service.payment.domain.PaymentGateway;
import com.amazon.payment_service.payment.domain.PaymentRepository;
import com.amazon.payment_service.payment.domain.exception.PaymentAlreadyPaidException;
import com.amazon.shared.core.domain.vo.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentCreator {

    private final PaymentEventPublisher paymentEventPublisher;
    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;

    @Transactional
    public void create(UUID orderId, Money amount) {
        Optional<Payment> existingPayment = paymentRepository.findByOrderId(orderId);

        if (existingPayment.isPresent()) {
            throw new PaymentAlreadyPaidException(existingPayment.get().id());
        }

        Payment payment = Payment.create(UUID.randomUUID(), orderId, amount);

        final Payment paymentProcessed = paymentGateway.process(payment);

        paymentRepository.save(paymentProcessed);
        paymentEventPublisher.publish(paymentProcessed);
    }
}
