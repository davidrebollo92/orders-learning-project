package com.amazon.service_b.payment.aplication;

import com.amazon.service_b.payment.domain.Payment;
import com.amazon.service_b.payment.domain.PaymentEventPublisher;
import com.amazon.service_b.payment.domain.PaymentRepository;
import com.amazon.service_b.payment.domain.Transaction;
import com.amazon.service_b.payment.domain.exception.PaymentAlreadyPaidException;
import com.amazon.service_b.shared.domain.vo.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentProcessor {

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher paymentEventPublisher;

    @Transactional
    public void process(Payment payment, Money amount) {
        if (paymentRepository.findById(payment.id()).isPresent()) {
            throw new PaymentAlreadyPaidException(payment.id());
        }

        Transaction transaction = Transaction.create(amount);
        Payment completedPayment = payment.pay(transaction);
        Payment savedPayment = paymentRepository.completePayment(completedPayment);

        paymentEventPublisher.publishPaymentCompleted(savedPayment);
    }
}
