# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Structure

This is a **Maven multi-module monorepo** with three modules:

```
amazon/
  pom.xml                — aggregator raíz (hereda de spring-boot-starter-parent)
  docker-compose.yml     — infraestructura + servicios Java (stack completo)
  service_boot/          — librería compartida (jar); Money, DomainException, KafkaTopicsConfig
  service_a/
    Dockerfile           — build multi-stage; contexto de build = raíz
  service_b/
    Dockerfile           — build multi-stage; contexto de build = raíz
```

`service_a` y `service_b` dependen de `service_boot`. Tras cualquier cambio en `service_boot` hay que reinstalar desde la raíz (`./service_a/mvnw install -DskipTests`) para que los servicios lo resuelvan.

## Commands

### Stack completo (recomendado)

```bash
# Arrancar toda la infraestructura + servicios (construye las imágenes si no existen)
docker compose up -d

# Reconstruir imágenes tras cambios en el código
docker compose up -d --build
```

### Desarrollo local (fuera de Docker)

From the **repo root** (`amazon/`):

```bash
# Instalar todos los módulos en el repositorio local de Maven
# (necesario la primera vez y cada vez que se modifique service_boot)
./mvnw install -DskipTests

# Solo infraestructura (PostgreSQL × 2 + Kafka), sin los servicios Java
docker compose up -d postgres-service-a postgres-service-b kafka
```

From each service directory (`service_a/` or `service_b/`):

```bash
# Build
./mvnw clean package

# Run
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Compile without running tests
./mvnw compile
```

## Architecture

Both services follow **Hexagonal Architecture** (Ports and Adapters) with **DDD bounded contexts**, built on **Spring Boot 4.0.6 / Java 21**.

### service_boot — Shared library

```
core/
  domain/vo/Money.java                          — value object compartido
  domain/exception/DomainException.java         — base de todas las excepciones de dominio
  infrastructure/messaging/KafkaTopicsConfig.java — configuración de topics Kafka
```

Paquete base: `com.amazon.service_boot.core`. No tiene `main` ni `application.yaml` — es una librería jar pura.

### service_a — Order management

Bounded contexts:
```
order/    — order creation and query; owns `orders` and `payments` tables via JPA cascade
            Payment is part of this context (order/domain/Payment), not a separate bounded context
shared/   — ErrorDto, GlobalExceptionHandler (específicos de service_a, no en service_boot)
```

API endpoints:
```
POST   /orders       → create order (returns 201)
GET    /orders       → list all orders (returns 200)
GET    /orders/{id}  → get order by id (returns 200)
```

### service_b — Payment processing

Bounded contexts:
```
payment/  — payment processing aggregate; owns `payments` and `transactions` tables
```

No HTTP endpoints — fully event-driven.

### Layer structure (both services)

```
{context}/
  domain/
    exception/     → DomainException hierarchy + error codes as string constants
    *.java         → domain model (immutable records) + port interfaces (no Spring, no JPA)
  aplication/      → service classes (e.g. OrderCreator, PaymentProcessor). No use case interfaces.
  infrastructure/
    http/
      dto/         → request/response DTOs
      mapper/      → HTTP mappers (@Component)
      *.java       → REST controller, exception handler
    persistence/
      mapper/      → persistence mappers (@Component)
      *.java       → JPA entities, JPA repositories, repository adapter
    messaging/
      *.java       → Kafka consumers, outbox publisher, topic config
    gateway/       → adapters for external services (e.g. SimulatedPaymentGateway) — service_b only
```

Note: `aplication` is a pre-existing typo — do not rename it.

### Ports and adapters pattern

- **Driving ports** (input) — controllers and Kafka consumers depend directly on service classes in `aplication/` (no use case interfaces).
- **Driven ports** (output) — repository, event publisher, and gateway interfaces in `domain/`. Services depend on these; adapters implement them.

```
# service_a
Controller → ServiceClass → OrderRepository (interface) ← OrderPostgreSqlRepository → JpaOrderRepository
                                                                                      → JpaOrderPaymentRepository
           → OrderEventPublisher (interface) ← OrderOutboxEventPublisher → JpaOutboxEventRepository
KafkaConsumer → PaymentCompleter → OrderRepository
             → OrderCanceller   → OrderRepository

# service_b
KafkaConsumer → PaymentProcessor → PaymentRepository (interface) ← PaymentPostgreSqlRepository → JpaPaymentRepository
                                 → PaymentEventPublisher (interface) ← PaymentOutboxEventPublisher → JpaOutboxEventRepository
                                 → PaymentGateway (interface) ← SimulatedPaymentGateway
```

### Event-driven flow

```
POST /orders (service_a)
  → OrderCreator
      → saves Order (CREATED) + Payment (PENDING) via JPA cascade → `orders` + `payments` tables
      → OrderOutboxEventPublisher.publishOrderCreated()
          → serializes OrderCreatedEvent as Avro JSON → saves to `outbox_events` (same transaction)
  → OutboxScheduler (every 1s)
      → reads unpublished outbox_events, deserializes Avro JSON → sends as Avro binary to topic `ordersCreated`, marks published

OrderCreatedKafkaEventConsumer (service_b)
  → PaymentProcessor
      → idempotency check: if payment already in `payments` → throw PaymentAlreadyPaidException → log warn, discard
      → PaymentGateway.charge(amount):
          happy path (amount ≤ 1000):
            → creates Transaction, pays Payment
            → saves Payment (PAID) + Transaction → `payments` + `transactions` tables
            → PaymentOutboxEventPublisher.publishPaymentCompleted()
                → serializes PaymentCompletedEvent as Avro JSON → saves to `outbox_events` (same transaction)
          failure path (amount > 1000 → InsufficientFundsException):
            → payment.fail() → Payment (FAILED)
            → saves Payment (FAILED) → `payments` table
            → PaymentOutboxEventPublisher.publishPaymentFailed()
                → serializes PaymentFailedEvent as Avro JSON → saves to `outbox_events` (same transaction)
  → OutboxScheduler (every 1s)
      → reads unpublished outbox_events, deserializes Avro JSON → sends as Avro binary to topic `paymentsCompleted` or `paymentsFailed`, marks published

PaymentKafkaEventConsumer (service_a) — two dedicated listeners, one per topic
  consumeCompleted (topic=paymentsCompleted) → PaymentCompleter
      → KafkaAvroDeserializer instantiates PaymentCompletedEvent (specific.avro.reader=true)
      → loads Order by orderId → if not found → log error (OrderNotFoundException), discard
      → checks paymentId matches order's payment → if not → log error (PaymentNotFoundException), discard
      → calls order.completePayment() → Order (PAID), payment.pay() → Payment (PAID)
      → calls orderRepository.updatePayment() → UPDATE `orders` SET state=PAID, UPDATE `payments` SET state=PAID
  consumeFailed (topic=paymentsFailed) → OrderCanceller
      → KafkaAvroDeserializer instantiates PaymentFailedEvent (specific.avro.reader=true)
      → loads Order by orderId → if not found → log error (OrderNotFoundException), discard
      → checks paymentId matches order's payment → if not → log error (PaymentNotFoundException), discard
      → calls order.cancel() → Order (CANCELLED), payment.fail() → Payment (FAILED)
      → calls orderRepository.updatePayment() → UPDATE `orders` SET state=CANCELLED, UPDATE `payments` SET state=FAILED
```

### Outbox Pattern

Both services use the **Transactional Outbox Pattern** to guarantee event publishing:

- `OrderOutboxEventPublisher` / `PaymentOutboxEventPublisher` — implement the domain port by saving the event as a row in `outbox_events` within the same transaction as the business operation. No direct Kafka write from the service.
- `OutboxEventEntity` — JPA entity (`outbox_events` table) with fields: `id`, `aggregateId`, `topic`, `payload` (Avro JSON), `eventType` (fully qualified Avro class name), `occurredAt`, `publishedAt` (null until sent).
- `OutboxScheduler` — `@Scheduled(fixedDelay = 1000)`. Queries `findByPublishedAtIsNull()`, deserializes the Avro JSON payload using `SpecificData.get().getSchema(clazz)` + `SpecificDatumReader`, sends each event via `KafkaTemplate<String, Object>` (serialized as Avro binary by `KafkaAvroSerializer`), then sets `publishedAt`. Runs in both services independently.
- `KafkaOutboxConfig` — defines the `@Bean("outboxKafkaTemplate")` with `StringSerializer` for key and `KafkaAvroSerializer` for value. The `eventType` column stores the fully qualified Avro class name (e.g. `com.amazon.avro.OrderCreatedEvent`) so the scheduler knows which schema to use for deserialization.

### Avro schemas and Kafka topics

Events are defined as **Avro schemas** (`src/main/avro/*.avsc`). The `avro-maven-plugin` generates Java classes in `target/generated-sources/avro/com/amazon/avro/` at `generate-sources` phase. Each service has its own copy of the schemas it uses (no cross-service imports). Schemas must be identical across services for Schema Registry compatibility.

**One event per topic** — each event type has a dedicated Kafka topic:

| Event | Topic (app.kafka.topics key) | Topic name |
|---|---|---|
| `OrderCreatedEvent` | `ordersCreated` | `amazon.env.order-management.orders.created.pub` |
| `PaymentCompletedEvent` | `paymentsCompleted` | `amazon.env.order-management.payments.completed.pub` |
| `PaymentFailedEvent` | `paymentsFailed` | `amazon.env.order-management.payments.failed.pub` |

Consumers use `KafkaAvroDeserializer` with `specific.avro.reader: true` — the deserializer reads the schema ID from the message (written by Schema Registry), fetches the schema, and instantiates the correct generated class. No manual type routing needed.

### Kafka error handling

Consumers use `@RetryableTopic` (3 attempts, exponential backoff 1s×2) with a `@DltHandler` for unrecoverable failures.

Within each attempt, known domain exceptions are caught and not retried:
- Duplicate event (`PaymentAlreadyPaidException`) → `log.warn`, discard.
- Payment/order not found (`PaymentNotFoundException`, `OrderNotFoundException`) → `log.error`, discard.
- Insufficient funds (`InsufficientFundsException`) → `log.warn`, discard (service_b).
- Any other exception propagates so `@RetryableTopic` retries it up to 3 times, then routes to DLT.

### Code style

- **Blank lines between logical steps** — separate distinct steps within a method with a blank line.

```java
// correct
Order created = orderRepository.create(order);

orderEventPublisher.publishOrderCreated(created);

return created;
```

### Domain rules

- **Domain objects are immutable records** — `Order`, `Payment`, `Transaction`, `Money` are Java records.
- **UUIDs generated in domain** — factory methods call `UUID.randomUUID()`. No `@GeneratedValue` in JPA entities.
- **`@Builder(toBuilder = true)`** — all domain records with `@Builder` use `toBuilder = true`. Mutation methods (e.g. `pay()`, `completePayment()`) must use `toBuilder()` to copy existing fields and only override what changes. Never use `builder()` from within a mutation method.
- **`Order.create(String name, Money amount)`** — static factory. Creates an `Order` with `state = CREATED` and `payment = null`. Call `addPayment()` to attach a `Payment` in `PENDING` state before persisting.
- **`payment/domain/Payment.create(UUID id, UUID orderId)`** — factory for new payments in `PENDING` state (service_b).
- **`Money`** lives in `service_boot/core/domain/vo/`. `isBelowMinimum()` returns `boolean`. Do not duplicate minimum-amount validation in DTOs.
- **HTTP mappers** and **persistence mappers** are `@Component` beans injected by constructor — never static.
- **DTOs** live in `infrastructure/http/dto/`. Domain objects are never serialized directly as API responses.
- **Variable names must match the class name** in camelCase (e.g. `OrderCreator orderCreator`).
- **Port naming** — domain interfaces have no suffix (e.g. `OrderRepository`, `PaymentEventPublisher`).
- **Adapter naming** — adapters include the technology (e.g. `OrderPostgreSqlRepository`, `PaymentOutboxEventPublisher`).
- **`@Transactional`** — always use `org.springframework.transaction.annotation.Transactional`, never `jakarta.transaction.Transactional`.
- **`@Enumerated(EnumType.STRING)`** — all enum fields in JPA entities must have this annotation. Without it, Hibernate 7 maps enums as `TINYINT` (ordinal), which conflicts with the `VARCHAR` columns declared in the Liquibase changelogs.

### Exception hierarchies

```
# service_boot (base)
DomainException (core/domain/exception)

# service_a
DomainException (service_boot)
  └── OrderDomainException (order)
        ├── OrderNotFoundException
        ├── InvalidOrderAmountException
        ├── PaymentNotFoundException
        └── PaymentAlreadyPaidException

# service_b
DomainException (service_boot)
  └── PaymentDomainException (payment)
        ├── PaymentAlreadyPaidException
        ├── InvalidPaymentStateException
        └── InsufficientFundsException
```

### Exception handling (service_a)

- `GlobalExceptionHandler` (service_a/shared/infrastructure) — handles `MethodArgumentNotValidException` and generic `Exception`.
- `OrderExceptionHandler` (order/infrastructure/http) — handles `OrderNotFoundException` (404) and `OrderDomainException` (400).
- All handlers return `ResponseEntity<ErrorDto>`. `ErrorDto` has `message` and `code` fields.

### HTTP responses (service_a)

- Controllers return `ResponseEntity<T>` directly.
- Success: `ResponseEntity.ok(body)` for 200, `ResponseEntity.status(HttpStatus.CREATED).body(body)` for 201.
- Errors: `ErrorDto(message, code)`.

### Infrastructure

- **Databases** (one per service):
  - `service_a_db` — PostgreSQL at `localhost:5432` (host) / `amazon-postgres-service-a:5432` (Docker) — user/pass: `postgres/postgres`
  - `service_b_db` — PostgreSQL at `localhost:5433` (host) / `amazon-postgres-service-b:5432` (Docker) — user/pass: `postgres/postgres`
- **Tables**:
  - `orders` → `OrderEntity` (service_a, order context) — columns: `id`, `name`, `amount`, `state`, `payment_id`
  - `payments` → `OrderPaymentEntity` (service_a, order context, cascade from `orders`) — columns: `id`, `state`
  - `outbox_events` → `OutboxEventEntity` (service_a, order context)
  - `payments` → `PaymentEntity` (service_b, payment context; different database from service_a) — columns: `id`, `order_id`, `state`, `transaction_id`
  - `transactions` → `TransactionEntity` (service_b, payment context, cascade from `payments`)
  - `outbox_events` → `OutboxEventEntity` (service_b, payment context)
  - All primary keys are `UUID` — no `@GeneratedValue`.
- **Messaging**: Kafka at `localhost:9092` (host) / `kafka:19092` (Docker). Topics configured via `app.kafka.topics` in `KafkaTopicsConfig` (`@ConfigurationProperties` + `@Component`). Topics are auto-created on startup.
- **Schema Registry**: Confluent Schema Registry at `localhost:8081` (host) / `schema-registry:8081` (Docker). URL configured via `spring.kafka.properties.schema.registry.url`. Used by `KafkaAvroSerializer` (producer) and `KafkaAvroDeserializer` (consumer) to register and resolve Avro schemas. In tests, `mock://test` replaces the real registry.
- **DB Schema**: managed by **Liquibase** (`ddl-auto: validate` — Hibernate only validates, never alters). Changelogs live in `src/main/resources/db/changelog/`. The master file `db.changelog-master.yaml` includes changesets from `changes/`. To add a schema change, create a new `NNN_description.sql` file and add an `include` entry to the master — never modify existing changesets.
  - service_a changelogs: `001_init_schema.sql` (orders, payments, outbox_events), `002_add_state_to_orders.sql` (adds `state VARCHAR(10)` to `orders`), `003_add_event_type_to_outbox.sql` (adds `event_type VARCHAR(255)` to `outbox_events`)
  - service_b changelogs: `001_init_schema.sql` (transactions, payments, outbox_events), `002_add_event_type_to_outbox.sql` (adds `event_type VARCHAR(255)` to `outbox_events`)
