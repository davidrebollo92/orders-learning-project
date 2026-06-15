# amazon

Proyecto de aprendizaje que implementa **Arquitectura Hexagonal** (Puertos y Adaptadores) con **DDD**, comunicación asíncrona vía **Kafka** y el patrón **Transactional Outbox**.

Proyecto Maven multi-módulo con tres módulos (Java 21 / Spring Boot 4.0.6):

- **service_boot** — librería compartida (`Money`, `DomainException`, `KafkaTopicsConfig`)
- **service_a** — gestión de pedidos (API REST + consumidor de eventos de pago)
- **service_b** — procesamiento de pagos (consumidor/productor de eventos, sin HTTP)

## Patrones implementados

- **Hexagonal Architecture** — dominio aislado de infraestructura mediante puertos e interfaces
- **DDD** — agregados, value objects, excepciones de dominio, factorías en el dominio
- **Transactional Outbox** — los eventos se serializan como Avro binario y se guardan en base de datos (`BYTEA`) dentro de la misma transacción del negocio; un scheduler los publica a Kafka, garantizando consistencia sin two-phase commit
- **Saga con compensación** — flujo distribuido coordinado por eventos; si el pago falla, service_b publica `PaymentFailedEvent` y service_a cancela la orden (`CANCELLED`)
- **Avro + Schema Registry** — los eventos Kafka se serializan con Apache Avro; los schemas (`.avsc`) son la fuente de verdad y se registran en Confluent Schema Registry; un topic por tipo de evento
- **Idempotencia** — los consumidores detectan y descartan eventos duplicados
- **Dead Letter Topic con persistencia** — tras agotar los reintentos, el evento se persiste en `dead_letter_events` (topic, payload Avro binario, tipo de evento, partición y offset originales) para su inspección y reprocesado manual
- **Migraciones con Liquibase** — el esquema de base de datos se gestiona mediante changelogs versionados; Hibernate solo valida, nunca altera

## Flujo

**Happy path (importe ≤ 1000):**
1. `POST /orders` crea una orden (`CREATED`) con un pago (`PENDING`) y guarda `OrderCreatedEvent` serializado como Avro binario (`BYTEA`) en `outbox_events` de forma atómica
2. El `OutboxScheduler` de service_a deserializa el payload binario y publica el evento al topic `ordersCreated`
3. service_b consume `OrderCreatedEvent`, cobra el pago vía `SimulatedPaymentGateway` y guarda `PaymentCompletedEvent` (Avro binario) en su `outbox_events`
4. El `OutboxScheduler` de service_b publica el evento al topic `paymentsCompleted`
5. service_a consume `PaymentCompletedEvent` y transiciona la orden a `PAID`

**Flujo de compensación (importe > 1000):**
1–2. Igual que arriba
3. service_b detecta fondos insuficientes, falla el pago y guarda `PaymentFailedEvent` (Avro binario) en su `outbox_events`
4. El `OutboxScheduler` de service_b publica el evento al topic `paymentsFailed`
5. service_a consume `PaymentFailedEvent` y transiciona la orden a `CANCELLED`

## Gestión de errores Kafka

Los consumidores usan `@RetryableTopic` (3 reintentos, backoff exponencial). Las excepciones de negocio conocidas se absorben sin reintentar (duplicados → `warn`, no encontrado → `error`, fondos insuficientes → `warn`). Tras agotar los reintentos, el mensaje va al Dead Letter Topic y el `@DltHandler` persiste el evento fallido en la tabla `dead_letter_events` (topic, payload Avro, tipo de evento, partición y offset originales) para su inspección y reprocesado manual.

## Requisitos

- Docker

## Arrancar

```bash
docker compose up -d
```

Los servicios se construyen y arrancan automáticamente junto con la infraestructura (PostgreSQL × 2 + Kafka + Schema Registry).

## Endpoints (service_a)

```
POST   /orders       — crear pedido        → 201
GET    /orders       — listar pedidos      → 200
GET    /orders/{id}  — obtener por id      → 200 / 404
```

## Tests

```bash
# Desde la raíz — todos los tests unitarios y de capa web
./mvnw test -pl service_boot,service_a,service_b

# Tests de integración (requieren Docker para Testcontainers + EmbeddedKafka)
./mvnw verify -pl service_a   # OrderIntegrationTest
./mvnw verify -pl service_b   # PaymentIntegrationTest
```

Capas cubiertas: dominio, servicios de aplicación, capa web (`@WebMvcTest`), mappers HTTP y de persistencia, consumers Kafka (unitario), e integración end-to-end con Testcontainers (PostgreSQL) y EmbeddedKafka.
