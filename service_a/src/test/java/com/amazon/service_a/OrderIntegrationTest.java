package com.amazon.service_a;

import com.amazon.service_a.order.domain.Payment;
import com.amazon.service_a.order.infrastructure.http.dto.CreateOrderRequest;
import com.amazon.service_a.order.infrastructure.persistence.JpaOrderRepository;
import com.amazon.service_a.order.infrastructure.persistence.OrderEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
)
@AutoConfigureMockMvc
@EmbeddedKafka(
        partitions = 1,
        topics = {
                "amazon.env.order-management.orders.pub",
                "amazon.env.order-management.payments.pub"
        }
)
class OrderIntegrationTest {

    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private JpaOrderRepository jpaOrderRepository;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @BeforeEach
    void cleanUp() {
        jpaOrderRepository.deleteAll();
    }

    @Test
    void createOrder_persistsOrderWithPendingPayment() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest("laptop", new BigDecimal("10.00"));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("laptop"))
                .andExpect(jsonPath("$.amount").value(10.00))
                .andExpect(jsonPath("$.payment.state").value("PENDING"));

        List<OrderEntity> orders = jpaOrderRepository.findAll();
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getPayment().getState()).isEqualTo(Payment.State.PENDING);
    }

    @Test
    void createOrder_publishesOrderCreatedEventToKafka() throws Exception {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                "test-group-producer", "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<String, String>(consumerProps)
                .createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(
                consumer, "amazon.env.order-management.orders.pub");

        CreateOrderRequest request = new CreateOrderRequest("laptop", new BigDecimal("10.00"));
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5));
        consumer.close();

        assertThat(records.count()).isEqualTo(1);
        String payload = records.iterator().next().value();
        assertThat(payload).contains("\"orderId\"");
        assertThat(payload).contains("\"paymentId\"");
        assertThat(payload).contains("\"amount\"");
    }

    @Test
    void getOrderById_returns200_whenOrderExists() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest("laptop", new BigDecimal("10.00"));
        String response = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String orderId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.name").value("laptop"));
    }

    @Test
    void getOrderById_returns404_whenOrderNotFound() throws Exception {
        mockMvc.perform(get("/orders/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"));
    }
}
