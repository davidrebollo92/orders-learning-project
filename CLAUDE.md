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
      dto/         → Kafka event DTOs (one per event type)
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
          → saves OrderCreatedEvent to `outbox_events` table (same transaction)
  → OutboxScheduler (every 1s)
      → reads unpublished outbox_events, sends to Kafka topic `orders`, marks published

OrderCreatedKafkaEventConsumer (service_b)
  → PaymentProcessor
      → idempotency check: if payment already in `payments` → throw PaymentAlreadyPaidException → log warn, discard
      → PaymentGateway.charge(amount):
          happy path (amount ≤ 1000):
            → creates Transaction, pays Payment
            → saves Payment (PAID) + Transaction → `payments` + `transactions` tables
            → PaymentOutboxEventPublisher.publishPaymentCompleted()
                → saves PaymentCompletedEvent (type=PAYMENT_COMPLETED) to `outbox_events` (same transaction)
          failure path (amount > 1000 → InsufficientFundsException):
            → payment.fail() → Payment (FAILED)
            → saves Payment (FAILED) → `payments` table
            → PaymentOutboxEventPublisher.publishPaymentFailed()
                → saves PaymentFailedEvent (type=PAYMENT_FAILED) to `outbox_events` (same transaction)
  → OutboxScheduler (every 1s)
      → reads unpublished outbox_events, sends to Kafka topic `payments`, marks published

PaymentKafkaEventConsumer (service_a) — deserializes as PaymentEvent, routes by `type`
  type=PAYMENT_COMPLETED → PaymentCompleter
      → loads Order by orderId → if not found → log error (OrderNotFoundException), discard
      → checks paymentId matches order's payment → if not → log error (PaymentNotFoundException), discard
      → calls order.completePayment() → Order (PAID), payment.pay() → Payment (PAID)
      → calls orderRepository.updatePayment() → UPDATE `orders` SET state=PAID, UPDATE `payments` SET state=PAID
  type=PAYMENT_FAILED → OrderCanceller
      → loads Order by orderId → if not found → log error (OrderNotFoundException), discard
      → checks paymentId matches order's payment → if not → log error (PaymentNotFoundException), discard
      → calls order.cancel() → Order (CANCELLED), payment.fail() → Payment (FAILED)
      → calls orderRepository.updatePayment() → UPDATE `orders` SET state=CANCELLED, UPDATE `payments` SET state=FAILED
```

### Outbox Pattern

Both services use the **Transactional Outbox Pattern** to guarantee event publishing:

- `OrderOutboxEventPublisher` / `PaymentOutboxEventPublisher` — implement the domain port by saving the event as a row in `outbox_events` within the same transaction as the business operation. No direct Kafka write from the service.
- `OutboxEventEntity` — JPA entity (`outbox_events` table) with fields: `id`, `aggregateId`, `topic`, `payload` (JSON), `occurredAt`, `publishedAt` (null until sent).
- `OutboxScheduler` — `@Scheduled(fixedDelay = 1000)`. Queries `findByPublishedAtIsNull()`, sends each event via `KafkaTemplate`, then sets `publishedAt`. Runs in both services independently.
- `KafkaOutboxConfig` — defines the `@Bean("outboxKafkaTemplate")` with `StringSerializer` for key and value. The payload is pre-serialized JSON stored in `outbox_events.payload`, so `StringSerializer` is required — using `JsonSerializer` would double-serialize it. As a consequence, Kafka messages sent by the Outbox carry no `__TypeId__` headers; consumers are configured with `spring.json.use.type.headers: false` and `spring.json.value.default.type` to deserialize without headers.

### Kafka event DTOs

Each service defines its own local event DTOs in `infrastructure/messaging/dto/` — no cross-service imports:
- `service_a/order/infrastructure/messaging/dto/` — `OrderCreatedEvent` (producer), `PaymentEvent` (consumer wrapper)
- `service_b/payment/infrastructure/messaging/dto/` — `OrderCreatedEvent` (consumer), `PaymentCompletedEvent` (producer), `PaymentFailedEvent` (producer)

Both `PaymentCompletedEvent` and `PaymentFailedEvent` are published to the same topic `payments` with a `type` field (`PAYMENT_COMPLETED` / `PAYMENT_FAILED`). service_a deserializes both as `PaymentEvent` (a temporary common type pending Schema Registry implementation) and routes by `type`.

All events include a `type` field for self-description. `PaymentEvent` in service_a is a temporary wrapper — when Schema Registry is implemented it will be replaced by `PaymentCompletedEvent` and `PaymentFailedEvent` with schema-based deserialization.

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
- **Schema**: managed by **Liquibase** (`ddl-auto: validate` — Hibernate only validates, never alters). Changelogs live in `src/main/resources/db/changelog/`. The master file `db.changelog-master.yaml` includes changesets from `changes/`. To add a schema change, create a new `NNN_description.sql` file and add an `include` entry to the master — never modify existing changesets.
  - service_a changelogs: `001_init_schema.sql` (orders, payments, outbox_events), `002_add_state_to_orders.sql` (adds `state VARCHAR(10)` column to `orders`)
  - service_b changelogs: `001_init_schema.sql` (transactions, payments, outbox_events)
