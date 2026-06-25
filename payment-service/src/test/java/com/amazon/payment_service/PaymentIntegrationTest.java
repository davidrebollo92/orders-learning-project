package com.amazon.payment_service;

import com.amazon.avro.OrderCreatedEvent;
import com.amazon.avro.PaymentCompletedEvent;
import com.amazon.payment_service.payment.domain.Payment;
import com.amazon.payment_service.payment.infrastructure.persistence.JpaDeadLetterEventRepository;
import com.amazon.payment_service.payment.infrastructure.persistence.JpaOutboxEventRepository;
import com.amazon.payment_service.payment.infrastructure.persistence.JpaPaymentRepository;
import com.amazon.payment_service.payment.infrastructure.persistence.entity.DeadLetterEventEntity;
import com.amazon.payment_service.payment.infrastructure.persistence.entity.PaymentEntity;
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
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest"
})
@EmbeddedKafka(
        partitions = 1,
        topics = {
                "amazon.env.order-management.orders.created.pub",
                "amazon.env.order-management.payments.completed.pub",
                "amazon.env.order-management.payments.failed.pub",
                "amazon.env.order-management.orders.created.pub-dlt"
        }
)
class PaymentIntegrationTest {

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

    @Autowired
    private JpaPaymentRepository jpaPaymentRepository;

    @Autowired
    private JpaOutboxEventRepository jpaOutboxEventRepository;

    @Autowired
    private JpaDeadLetterEventRepository jpaDeadLetterEventRepository;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @BeforeEach
    void cleanUp() {
        jpaDeadLetterEventRepository.deleteAll();
        jpaOutboxEventRepository.deleteAll();
        jpaPaymentRepository.deleteAll();
    }

    @Test
    void consumeOrderCreated_savesPaymentAndPublishesPaymentCompletedEvent() {
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                "test-group-consumer", "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        consumerProps.put("schema.registry.url", MOCK_SCHEMA_REGISTRY);
        consumerProps.put("specific.avro.reader", "true");
        Consumer<String, Object> consumer = new DefaultKafkaConsumerFactory<String, Object>(consumerProps)
                .createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(
                consumer, "amazon.env.order-management.payments.completed.pub");

        publishOrderCreatedEvent(orderId, paymentId, new BigDecimal("50.00"));

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            Optional<PaymentEntity> payment = jpaPaymentRepository.findById(paymentId);
            assertThat(payment).isPresent();
            assertThat(payment.get().getState()).isEqualTo(Payment.State.PAID);
            assertThat(payment.get().getOrderId()).isEqualTo(orderId);
        });

        ConsumerRecords<String, Object> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5));
        consumer.close();

        assertThat(records.count()).isEqualTo(1);
        PaymentCompletedEvent event = (PaymentCompletedEvent) records.iterator().next().value();
        assertThat(event.getPaymentId()).isEqualTo(paymentId.toString());
        assertThat(event.getOrderId()).isEqualTo(orderId.toString());
    }

    @Test
    void consumeOrderCreated_isIdempotent_whenSameEventConsumedTwice() {
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        publishOrderCreatedEvent(orderId, paymentId, new BigDecimal("50.00"));
        publishOrderCreatedEvent(orderId, paymentId, new BigDecimal("50.00"));

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            List<PaymentEntity> payments = jpaPaymentRepository.findAll();
            assertThat(payments).hasSize(1);
            assertThat(payments.get(0).getId()).isEqualTo(paymentId);
        });
    }

    @Test
    void orderCreatedEvent_savesToDeadLetterWhenRoutedToDlt() {
        OrderCreatedEvent event = OrderCreatedEvent.newBuilder()
                .setOrderId(UUID.randomUUID().toString())
                .setPaymentId(UUID.randomUUID().toString())
                .setAmount("100.00")
                .build();

        publishToDltTopic(event, "amazon.env.order-management.orders.created.pub-dlt");

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            List<DeadLetterEventEntity> dltEvents = jpaDeadLetterEventRepository.findAll();
            assertThat(dltEvents).hasSize(1);
            assertThat(dltEvents.get(0).getEventType()).isEqualTo(OrderCreatedEvent.class.getName());
            assertThat(dltEvents.get(0).getTopic()).isEqualTo("amazon.env.order-management.orders.created.pub-dlt");
            assertThat(dltEvents.get(0).getPayload()).isNotEmpty();
        });
    }

    private void publishOrderCreatedEvent(UUID orderId, UUID paymentId, BigDecimal amount) {
        OrderCreatedEvent event = OrderCreatedEvent.newBuilder()
                .setOrderId(orderId.toString())
                .setAmount(amount.toPlainString())
                .setPaymentId(paymentId.toString())
                .build();

        publishToDltTopic(event, "amazon.env.order-management.orders.created.pub");
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
