package com.amazon.order_service.order.infrastructure.http;

import com.amazon.order_service.order.aplication.OrderCreator;
import com.amazon.order_service.order.aplication.OrderFinder;
import com.amazon.order_service.order.domain.Order;
import com.amazon.order_service.order.domain.exception.InvalidOrderAmountException;
import com.amazon.order_service.order.domain.exception.OrderNotFoundException;
import com.amazon.order_service.order.infrastructure.http.dto.CreateOrderRequest;
import com.amazon.order_service.order.infrastructure.http.dto.OrderResponse;
import com.amazon.order_service.order.infrastructure.http.dto.PaymentResponse;
import com.amazon.order_service.order.infrastructure.http.mapper.OrderDtoMapper;
import com.amazon.shared.core.domain.vo.Money;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private OrderCreator orderCreator;

    @MockitoBean
    private OrderFinder orderFinder;

    @MockitoBean
    private OrderDtoMapper orderDtoMapper;

    @Test
    void create_returns201WithOrderResponse_whenRequestIsValid() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        CreateOrderRequest request = new CreateOrderRequest("laptop", new BigDecimal("10.00"));
        Order order = Order.create("laptop", new Money(new BigDecimal("10.00")));
        Order orderWithPayment = order.addPayment();
        OrderResponse response = new OrderResponse(orderId, "laptop", new BigDecimal("10.00"),
                OrderResponse.StateEnum.CREATED, new PaymentResponse(paymentId, PaymentResponse.StateEnum.PENDING));

        when(orderDtoMapper.toDomain(any(CreateOrderRequest.class))).thenReturn(order);
        when(orderCreator.create(any(Order.class))).thenReturn(orderWithPayment);
        when(orderDtoMapper.toResponse(any(Order.class))).thenReturn(response);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.name").value("laptop"))
                .andExpect(jsonPath("$.amount").value(10.00))
                .andExpect(jsonPath("$.payment.state").value("PENDING"));
    }

    @Test
    void create_returns400_whenNameIsBlank() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest("", new BigDecimal("10.00"));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void create_returns400_whenAmountIsNull() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"laptop\",\"amount\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void create_returns400_whenAmountIsBelowMinimum() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest("laptop", BigDecimal.ZERO);

        when(orderDtoMapper.toDomain(any(CreateOrderRequest.class)))
                .thenThrow(new InvalidOrderAmountException());

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ORDER_AMOUNT"));
    }

    @Test
    void getAll_returns200WithOrderList() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        Order order = Order.create("laptop", new Money(new BigDecimal("10.00")));
        OrderResponse response = new OrderResponse(orderId, "laptop", new BigDecimal("10.00"),
                OrderResponse.StateEnum.CREATED, new PaymentResponse(paymentId, PaymentResponse.StateEnum.PENDING));

        when(orderFinder.findAll()).thenReturn(List.of(order));
        when(orderDtoMapper.toResponse(any(Order.class))).thenReturn(response);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(orderId.toString()))
                .andExpect(jsonPath("$[0].name").value("laptop"))
                .andExpect(jsonPath("$[0].payment.state").value("PENDING"));
    }

    @Test
    void getAll_returns200WithEmptyList() throws Exception {
        when(orderFinder.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void get_returns200WithOrderResponse_whenOrderFound() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        Order order = Order.create("laptop", new Money(new BigDecimal("10.00")));
        OrderResponse response = new OrderResponse(orderId, "laptop", new BigDecimal("10.00"),
                OrderResponse.StateEnum.CREATED, new PaymentResponse(paymentId, PaymentResponse.StateEnum.PENDING));

        when(orderFinder.findById(orderId)).thenReturn(order);
        when(orderDtoMapper.toResponse(any(Order.class))).thenReturn(response);

        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.name").value("laptop"));
    }

    @Test
    void get_returns404_whenOrderNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderFinder.findById(id)).thenThrow(new OrderNotFoundException(id));

        mockMvc.perform(get("/orders/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"));
    }
}
