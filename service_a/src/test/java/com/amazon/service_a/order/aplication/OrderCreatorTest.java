package com.amazon.service_a.order.aplication;

import com.amazon.service_a.order.domain.Order;
import com.amazon.service_a.order.domain.OrderEventPublisher;
import com.amazon.service_a.order.domain.OrderRepository;
import com.amazon.service_boot.core.domain.vo.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderCreatorTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderEventPublisher orderEventPublisher;

    @InjectMocks
    private OrderCreator orderCreator;

    @Test
    void create_savesOrderWithPaymentAndPublishesEvent() {
        Order order = Order.create("laptop", new Money(new BigDecimal("10.00")));
        Order savedOrder = order.addPayment();
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        Order result = orderCreator.create(order);

        assertThat(result).isEqualTo(savedOrder);
        verify(orderRepository).save(any(Order.class));
        verify(orderEventPublisher).publishOrderCreated(savedOrder);
    }
}
