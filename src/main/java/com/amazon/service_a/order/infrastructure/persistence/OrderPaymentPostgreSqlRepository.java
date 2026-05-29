package com.amazon.service_a.order.infrastructure.persistence;

import com.amazon.service_a.order.domain.Payment;
import com.amazon.service_a.order.domain.PaymentRepository;
import com.amazon.service_a.order.domain.exception.PaymentAlreadyCompletedException;
import com.amazon.service_a.order.domain.exception.PaymentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OrderPaymentPostgreSqlRepository implements PaymentRepository {

    private final JpaOrderPaymentRepository jpaOrderPaymentRepository;

    @Override
    @Transactional
    public void completePayment(UUID paymentId) {
        OrderPaymentEntity entity = jpaOrderPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        if (entity.getState() == Payment.State.PAID) {
            throw new PaymentAlreadyCompletedException(paymentId);
        }

        jpaOrderPaymentRepository.updateState(paymentId, Payment.State.PAID);
    }
}
