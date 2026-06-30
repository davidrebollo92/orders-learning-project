package com.amazon.order_service.order.aplication;

import com.amazon.order_service.order.domain.Order;
import com.amazon.order_service.order.domain.OrderEventPublisher;
import com.amazon.order_service.order.domain.OrderRepository;
import com.amazon.order_service.order.domain.ProductData;
import com.amazon.order_service.order.domain.ProductGateway;
import com.amazon.shared.core.domain.vo.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

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

    @Mock
    private ProductGateway productGateway;

    @InjectMocks
    private OrderCreator orderCreator;

    @Test
    void create_savesOrderWithPaymentAndPublishesEvent() {
        UUID productId = UUID.randomUUID();
        int quantity = 2;
        Money price = new Money(new BigDecimal("10.00"));
        ProductData productData = new ProductData(productId, price);
        Order savedOrder = Order.create(productId, quantity, price).addPayment();

        when(productGateway.findById(productId)).thenReturn(productData);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        Order result = orderCreator.create(productId, quantity);

        assertThat(result).isEqualTo(savedOrder);
        verify(orderRepository).save(any(Order.class));
        verify(orderEventPublisher).publishOrderCreated(savedOrder);
    }
}
