package com.amazon.order_service.order.aplication;

import com.amazon.order_service.order.domain.Order;
import com.amazon.order_service.order.domain.OrderRepository;
import com.amazon.order_service.order.domain.exception.OrderNotFoundException;
import com.amazon.order_service.order.domain.exception.PaymentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderCanceller {

    private final OrderRepository orderRepository;

    @Transactional
    public void cancel(UUID orderId, UUID paymentId) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);

        if (optionalOrder.isEmpty()) {
            throw new OrderNotFoundException(orderId);
        }

        if (!optionalOrder.get().payment().id().equals(paymentId)) {
            throw new PaymentNotFoundException(paymentId);
        }

        final Order cancelledOrder = optionalOrder.get().cancel();

        orderRepository.updatePayment(cancelledOrder);
    }
}
