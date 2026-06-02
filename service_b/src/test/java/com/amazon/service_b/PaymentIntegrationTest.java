package com.amazon.service_b;

import com.amazon.service_b.payment.domain.Payment;
import com.amazon.service_b.payment.infrastructure.messaging.OrderCreatedEvent;
import com.amazon.service_b.payment.infrastructure.persistence.JpaPaymentRepository;
import com.amazon.service_b.payment.infrastructure.persistence.PaymentEntity;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@EmbeddedKafka(
        partitions = 1,
        topics = {
                "amazon.env.order-management.orders.pub",
                "amazon.env.order-management.payments.pub"
        }
)
class PaymentIntegrationTest {

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
    private JpaPaymentRepository jpaPaymentRepository;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @BeforeEach
    void cleanUp() {
        jpaPaymentRepository.deleteAll();
    }

    @Test
    void consumeOrderCreated_savesPaymentAndPublishesPaymentCompletedEvent() {
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                "test-group-consumer", "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<String, String>(consumerProps)
                .createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(
                consumer, "amazon.env.order-management.payments.pub");

        publishOrderCreatedEvent(orderId, paymentId, new BigDecimal("50.00"));

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            Optional<PaymentEntity> payment = jpaPaymentRepository.findById(paymentId);
            assertThat(payment).isPresent();
            assertThat(payment.get().getState()).isEqualTo(Payment.State.PAID);
            assertThat(payment.get().getOrderId()).isEqualTo(orderId);
        });

        ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5));
        consumer.close();

        assertThat(records.count()).isEqualTo(1);
        String payload = records.iterator().next().value();
        assertThat(payload).contains("\"paymentId\"");
        assertThat(payload).contains("\"orderId\"");
    }

    private void publishOrderCreatedEvent(UUID orderId, UUID paymentId, BigDecimal amount) {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        KafkaTemplate<String, OrderCreatedEvent> template = new KafkaTemplate<>(
                new DefaultKafkaProducerFactory<>(producerProps));

        OrderCreatedEvent event = new OrderCreatedEvent("ORDER_CREATED", orderId, amount, paymentId);
        ProducerRecord<String, OrderCreatedEvent> record = new ProducerRecord<>(
                "amazon.env.order-management.orders.pub", orderId.toString(), event);

        template.send(record);
        template.flush();
    }
}
