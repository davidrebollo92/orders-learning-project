package com.amazon.order_service.order.infrastructure.http;

import com.amazon.order_service.order.aplication.OrderCreator;
import com.amazon.order_service.order.aplication.OrderFinder;
import com.amazon.order_service.order.domain.Order;
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
import static org.mockito.ArgumentMatchers.eq;
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

    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final UUID PAYMENT_ID = UUID.randomUUID();

    @Test
    void create_returns201WithOrderResponse_whenRequestIsValid() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(PRODUCT_ID, 2);
        Order orderWithPayment = Order.create(PRODUCT_ID, 2, new Money(new BigDecimal("20.00")));
        OrderResponse response = new OrderResponse(ORDER_ID, PRODUCT_ID, 2, new BigDecimal("20.00"),
                OrderResponse.StateEnum.CREATED, new PaymentResponse(PAYMENT_ID, PaymentResponse.StateEnum.PENDING));

        when(orderCreator.create(eq(PRODUCT_ID), eq(2))).thenReturn(orderWithPayment);
        when(orderDtoMapper.toResponse(any(Order.class))).thenReturn(response);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ORDER_ID.toString()))
                .andExpect(jsonPath("$.productId").value(PRODUCT_ID.toString()))
                .andExpect(jsonPath("$.amount").value(20.00))
                .andExpect(jsonPath("$.payment.state").value("PENDING"));
    }

    @Test
    void create_returns400_whenQuantityIsNull() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + PRODUCT_ID + "\",\"quantity\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void getAll_returns200WithOrderList() throws Exception {
        Order order = Order.create(PRODUCT_ID, 2, new Money(new BigDecimal("20.00")));
        OrderResponse response = new OrderResponse(ORDER_ID, PRODUCT_ID, 2, new BigDecimal("20.00"),
                OrderResponse.StateEnum.CREATED, new PaymentResponse(PAYMENT_ID, PaymentResponse.StateEnum.PENDING));

        when(orderFinder.findAll()).thenReturn(List.of(order));
        when(orderDtoMapper.toResponse(any(Order.class))).thenReturn(response);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ORDER_ID.toString()))
                .andExpect(jsonPath("$[0].productId").value(PRODUCT_ID.toString()))
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
        Order order = Order.create(PRODUCT_ID, 2, new Money(new BigDecimal("20.00")));
        OrderResponse response = new OrderResponse(ORDER_ID, PRODUCT_ID, 2, new BigDecimal("20.00"),
                OrderResponse.StateEnum.CREATED, new PaymentResponse(PAYMENT_ID, PaymentResponse.StateEnum.PENDING));

        when(orderFinder.findById(ORDER_ID)).thenReturn(order);
        when(orderDtoMapper.toResponse(any(Order.class))).thenReturn(response);

        mockMvc.perform(get("/orders/{id}", ORDER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ORDER_ID.toString()))
                .andExpect(jsonPath("$.productId").value(PRODUCT_ID.toString()));
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
