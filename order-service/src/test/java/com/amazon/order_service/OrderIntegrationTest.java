package com.amazon.order_service;

import com.amazon.avro.OrderCreatedEvent;
import com.amazon.avro.PaymentCompletedEvent;
import com.amazon.avro.PaymentFailedEvent;
import com.amazon.order_service.order.domain.Payment;
import com.amazon.order_service.order.domain.ProductData;
import com.amazon.order_service.order.domain.ProductGateway;
import com.amazon.order_service.order.infrastructure.http.dto.CreateOrderRequest;
import com.amazon.shared.core.domain.vo.Money;
import com.amazon.order_service.order.infrastructure.persistence.JpaDeadLetterEventRepository;
import com.amazon.order_service.order.infrastructure.persistence.JpaOrderRepository;
import com.amazon.order_service.order.infrastructure.persistence.JpaOutboxEventRepository;
import com.amazon.order_service.order.infrastructure.persistence.entity.DeadLetterEventEntity;
import com.amazon.order_service.order.infrastructure.persistence.entity.OrderEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.subject.TopicRecordNameStrategy;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
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
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
                "spring.kafka.consumer.auto-offset-reset=earliest",
                "app.inventory-service.base-url=http://localhost:8083"
        }
)
@AutoConfigureMockMvc
@EmbeddedKafka(
        partitions = 1,
        topics = {
                "amazon.env.order-management.orders.created.pub",
                "amazon.env.order-management.payments.completed.pub",
                "amazon.env.order-management.payments.failed.pub",
                "amazon.env.order-management.payments.completed.pub-dlt",
                "amazon.env.order-management.payments.failed.pub-dlt"
        }
)
class OrderIntegrationTest {

    private static final String MOCK_SCHEMA_REGISTRY = "mock://test";

    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.properties.schema.registry.url", () -> MOCK_SCHEMA_REGISTRY);
    }

    private static final UUID PRODUCT_ID = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
    private static final Money PRODUCT_PRICE = new Money(new BigDecimal("10.00"));

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ProductGateway productGateway;

    @Autowired
    private JpaOrderRepository jpaOrderRepository;

    @Autowired
    private JpaOutboxEventRepository jpaOutboxEventRepository;

    @Autowired
    private JpaDeadLetterEventRepository jpaDeadLetterEventRepository;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @BeforeEach
    void setUp() {
        jpaDeadLetterEventRepository.deleteAll();
        jpaOutboxEventRepository.deleteAll();
        jpaOrderRepository.deleteAll();
        when(productGateway.findById(any())).thenReturn(new ProductData(PRODUCT_ID, PRODUCT_PRICE));
    }

    @Test
    void createOrder_persistsOrderWithPendingPayment() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(PRODUCT_ID, 2);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(PRODUCT_ID.toString()))
                .andExpect(jsonPath("$.quantity").value(2))
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
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        consumerProps.put("schema.registry.url", MOCK_SCHEMA_REGISTRY);
        consumerProps.put("specific.avro.reader", "true");
        Consumer<String, Object> consumer = new DefaultKafkaConsumerFactory<String, Object>(consumerProps)
                .createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(
                consumer, "amazon.env.order-management.orders.created.pub");

        CreateOrderRequest request = new CreateOrderRequest(PRODUCT_ID, 2);
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        ConsumerRecords<String, Object> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5));
        consumer.close();

        assertThat(records.count()).isEqualTo(1);
        OrderCreatedEvent event = (OrderCreatedEvent) records.iterator().next().value();
        assertThat(event.getOrderId()).isNotNull();
        assertThat(event.getPaymentId()).isNotNull();
        assertThat(event.getAmount()).isNotNull();
        assertThat(event.getProductId()).isEqualTo(PRODUCT_ID.toString());
        assertThat(event.getQuantity()).isEqualTo(2);
    }

    @Test
    void getOrderById_returns200_whenOrderExists() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(PRODUCT_ID, 2);
        String response = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String orderId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.productId").value(PRODUCT_ID.toString()));
    }

    @Test
    void getOrderById_returns404_whenOrderNotFound() throws Exception {
        mockMvc.perform(get("/orders/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"));
    }

    @Test
    void getAll_returnsAllCreatedOrders() throws Exception {
        CreateOrderRequest request1 = new CreateOrderRequest(PRODUCT_ID, 1);
        CreateOrderRequest request2 = new CreateOrderRequest(PRODUCT_ID, 2);

        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void paymentCompletedEvent_savesToDeadLetterWhenRoutedToDlt() {
        PaymentCompletedEvent event = PaymentCompletedEvent.newBuilder()
                .setOrderId(UUID.randomUUID().toString())
                .setPaymentId(UUID.randomUUID().toString())
                .build();

        publishToDltTopic(event, "amazon.env.order-management.payments.completed.pub-dlt");

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            List<DeadLetterEventEntity> dltEvents = jpaDeadLetterEventRepository.findAll();
            assertThat(dltEvents).hasSize(1);
            assertThat(dltEvents.get(0).getEventType()).isEqualTo(PaymentCompletedEvent.class.getName());
            assertThat(dltEvents.get(0).getTopic()).isEqualTo("amazon.env.order-management.payments.completed.pub-dlt");
            assertThat(dltEvents.get(0).getPayload()).isNotEmpty();
        });
    }

    @Test
    void paymentFailedEvent_savesToDeadLetterWhenRoutedToDlt() {
        PaymentFailedEvent event = PaymentFailedEvent.newBuilder()
                .setOrderId(UUID.randomUUID().toString())
                .setPaymentId(UUID.randomUUID().toString())
                .build();

        publishToDltTopic(event, "amazon.env.order-management.payments.failed.pub-dlt");

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            List<DeadLetterEventEntity> dltEvents = jpaDeadLetterEventRepository.findAll();
            assertThat(dltEvents).hasSize(1);
            assertThat(dltEvents.get(0).getEventType()).isEqualTo(PaymentFailedEvent.class.getName());
            assertThat(dltEvents.get(0).getTopic()).isEqualTo("amazon.env.order-management.payments.failed.pub-dlt");
            assertThat(dltEvents.get(0).getPayload()).isNotEmpty();
        });
    }

    private void publishToDltTopic(SpecificRecord event, String topic) {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        producerProps.put("schema.registry.url", MOCK_SCHEMA_REGISTRY);
        producerProps.put("value.subject.name.strategy", TopicRecordNameStrategy.class.getName());

        KafkaTemplate<String, SpecificRecord> template = new KafkaTemplate<>(
                new DefaultKafkaProducerFactory<>(producerProps));

        template.send(new ProducerRecord<>(topic, UUID.randomUUID().toString(), event));
        template.flush();
    }
}
