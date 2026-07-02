package com.amazon.payment_service.payment.aplication;

import com.amazon.payment_service.payment.domain.Payment;
import com.amazon.payment_service.payment.domain.PaymentEventPublisher;
import com.amazon.payment_service.payment.domain.PaymentGateway;
import com.amazon.payment_service.payment.domain.PaymentRepository;
import com.amazon.payment_service.payment.domain.exception.PaymentAlreadyPaidException;
import com.amazon.shared.core.domain.vo.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentCreatorTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentEventPublisher paymentEventPublisher;

    @Mock
    private PaymentGateway paymentGateway;

    @InjectMocks
    private PaymentCreator paymentCreator;

    @Test
    void create_savesPaymentAndPublishesEvent_whenPaymentIsNew() {
        UUID orderId = UUID.randomUUID();
        Money amount = new Money(new BigDecimal("50.00"));
        Payment paidPayment = Payment.create(UUID.randomUUID(), orderId, amount).pay(UUID.randomUUID());

        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(paymentGateway.process(any(Payment.class))).thenReturn(paidPayment);
        when(paymentRepository.save(paidPayment)).thenReturn(paidPayment);

        paymentCreator.create(orderId, amount);

        verify(paymentRepository).save(paidPayment);
        verify(paymentEventPublisher).publish(paidPayment);
    }

    @Test
    void create_savesFailedPaymentAndPublishesEvent_whenGatewayFails() {
        UUID orderId = UUID.randomUUID();
        Money amount = new Money(new BigDecimal("1500.00"));
        Payment failedPayment = Payment.create(UUID.randomUUID(), orderId, amount).fail();

        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(paymentGateway.process(any(Payment.class))).thenReturn(failedPayment);
        when(paymentRepository.save(failedPayment)).thenReturn(failedPayment);

        paymentCreator.create(orderId, amount);

        verify(paymentRepository).save(failedPayment);
        verify(paymentEventPublisher).publish(failedPayment);
    }

    @Test
    void create_throwsPaymentAlreadyPaidException_whenPaymentForOrderAlreadyExists() {
        UUID orderId = UUID.randomUUID();
        Money amount = new Money(new BigDecimal("50.00"));
        Payment existing = Payment.create(UUID.randomUUID(), orderId, amount);

        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> paymentCreator.create(orderId, amount))
                .isInstanceOf(PaymentAlreadyPaidException.class);
    }
}
