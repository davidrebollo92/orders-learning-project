package com.amazon.order_service.order.aplication;

import com.amazon.order_service.order.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderCreator {

    private final OrderRepository orderRepository;
    private final ProductGateway productGateway;
    private final OrderEventPublisher orderEventPublisher;

    @Transactional
    public Order create(UUID productId, int quantity) {
        ProductData productData = productGateway.findById(productId);
        
        Order order = Order.create(productId, quantity, productData.price());

        Order created = orderRepository.save(order.addPayment());

        orderEventPublisher.publishOrderCreated(created);

        return created;
    }
}
