package com.amazon.service_a.order.aplication;

import com.amazon.service_a.order.domain.Order;
import com.amazon.service_a.order.domain.OrderRepository;
import com.amazon.service_a.order.domain.exception.OrderNotFoundException;
import com.amazon.service_a.order.domain.exception.PaymentNotFoundException;
import com.amazon.service_boot.core.domain.vo.Money;
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
class PaymentCompleterTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentCompleter paymentCompleter;

    @Test
    void complete_updatesOrderPayment_whenOrderAndPaymentMatch() {
        Order order = Order.create("laptop", new Money(new BigDecimal("10.00"))).addPayment();
        when(orderRepository.findById(order.id())).thenReturn(Optional.of(order));

        paymentCompleter.complete(order.id(), order.payment().id());

        verify(orderRepository).updatePayment(any(Order.class));
    }

    @Test
    void complete_throwsOrderNotFoundException_whenOrderNotFound() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentCompleter.complete(orderId, UUID.randomUUID()))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void complete_throwsPaymentNotFoundException_whenPaymentIdDoesNotMatch() {
        Order order = Order.create("laptop", new Money(new BigDecimal("10.00"))).addPayment();
        when(orderRepository.findById(order.id())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> paymentCompleter.complete(order.id(), UUID.randomUUID()))
                .isInstanceOf(PaymentNotFoundException.class);
    }
}
