# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Structure

This is a **monorepo** with two Spring Boot services:

```
amazon/
  service_a/   — order management service (HTTP API + Kafka producer/consumer)
  service_b/   — payment processing service (Kafka consumer/producer)
  docker-compose.yml
```

## Commands

Run from each service directory (`service_a/` or `service_b/`):

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

Start infrastructure (PostgreSQL × 2 + Kafka):

```bash
docker compose up -d
```

## Architecture

Both services follow **Hexagonal Architecture** (Ports and Adapters) with **DDD bounded contexts**, built on **Spring Boot 4.0.6 / Java 21**.

### service_a — Order management

Bounded contexts:
```
order/    — order creation and query; owns `orders` and `payments` tables via JPA cascade
            Payment is part of this context (order/domain/Payment), not a separate bounded context
shared/   — Money value object, DomainException, ErrorDto, KafkaTopicsConfig
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
payment/  — payment processing aggregate; owns `processed_payments` and `transactions` tables
shared/   — Money value object, DomainException, KafkaTopicsConfig
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
    messaging/     → Kafka adapters, event DTOs and topic configuration
```

Note: `aplication` is a pre-existing typo — do not rename it.

### Ports and adapters pattern

- **Driving ports** (input) — controllers and Kafka consumers depend directly on service classes in `aplication/` (no use case interfaces).
- **Driven ports** (output) — repository and event publisher interfaces in `domain/`. Services depend on these; adapters implement them.

```
# service_a
Controller → ServiceClass → OrderRepository (interface) ← OrderPostgreSqlRepository → JpaOrderRepository
                                                                                      → JpaOrderPaymentRepository
           → OrderEventPublisher (interface) ← OrderKafkaEventPublisher → KafkaTemplate
KafkaConsumer → PaymentCompleter → OrderRepository

# service_b
KafkaConsumer → PaymentProcessor → PaymentRepository (interface) ← PaymentPostgreSqlRepository → JpaPaymentRepository
                                 → PaymentEventPublisher (interface) ← PaymentKafkaEventPublisher → KafkaTemplate
```

### Event-driven flow

```
POST /orders (service_a)
  → OrderCreator
      → saves Order + Payment (PENDING) via JPA cascade → `orders` + `payments` tables
      → publishes OrderCreatedEvent → Kafka topic `orders`

OrderCreatedKafkaEventConsumer (service_b)
  → PaymentProcessor
      → idempotency check: if payment already in `processed_payments` → log warn, discard
      → creates Transaction, pays Payment
      → saves Payment (PAID) + Transaction → `processed_payments` + `transactions` tables
      → publishes PaymentCompletedEvent → Kafka topic `payments`

PaymentCompletedKafkaEventConsumer (service_a)
  → PaymentCompleter
      → loads Order by orderId → if not found → log error (OrderNotFoundException), discard
      → checks paymentId matches order's payment → if not → log error (PaymentNotFoundException), discard
      → calls order.completePayment() → payment.pay() → if already PAID → log warn (PaymentAlreadyPaidException), discard
      → calls orderRepository.updatePayment() → JpaOrderPaymentRepository.updateState() → UPDATE `payments` SET state=PAID
```

### Kafka event DTOs

Each service defines its own local event DTOs — no cross-service imports:
- `service_a/order/infrastructure/messaging/` — `OrderCreatedEvent` (producer), `PaymentCompletedEvent` (consumer)
- `service_b/payment/infrastructure/messaging/` — `OrderCreatedEvent` (consumer), `PaymentCompletedEvent` (producer)

### Kafka error handling

Kafka consumers catch known domain exceptions and log them without retrying:
- Duplicate event (`PaymentAlreadyPaidException`) → `log.warn`, discard.
- Payment/order not found (`PaymentNotFoundException`, `OrderNotFoundException`) → `log.error`, discard.
- Any other exception propagates to Kafka for retry.

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
- **`Order.create(String name, Money amount)`** — static factory. Creates an `Order` with a linked `Payment` in state `PENDING`.
- **`payment/domain/Payment.create(UUID id, UUID orderId)`** — factory for new payments in `PENDING` state (service_b).
- **`Money`** lives in `shared/domain/vo/`. `isBelowMinimum()` returns `boolean`. Do not duplicate minimum-amount validation in DTOs.
- **HTTP mappers** and **persistence mappers** are `@Component` beans injected by constructor — never static.
- **DTOs** live in `infrastructure/http/dto/`. Domain objects are never serialized directly as API responses.
- **Variable names must match the class name** in camelCase (e.g. `OrderCreator orderCreator`).
- **Port naming** — domain interfaces have no suffix (e.g. `OrderRepository`, `PaymentEventPublisher`).
- **Adapter naming** — adapters include the technology (e.g. `OrderPostgreSqlRepository`, `PaymentKafkaEventPublisher`).
- **`@Transactional`** — always use `org.springframework.transaction.annotation.Transactional`, never `jakarta.transaction.Transactional`.

### Exception hierarchies

```
# service_a
DomainException (shared)
  └── OrderDomainException (order)
        ├── OrderNotFoundException
        ├── InvalidOrderAmountException
        ├── PaymentNotFoundException
        └── PaymentAlreadyPaidException

# service_b
DomainException (shared)
  └── PaymentDomainException (payment)
        ├── PaymentAlreadyPaidException
        └── InvalidPaymentStateException
```

### Exception handling (service_a)

- `GlobalExceptionHandler` (shared/infrastructure) — handles `MethodArgumentNotValidException` and generic `Exception`.
- `OrderExceptionHandler` (order/infrastructure/http) — handles `OrderNotFoundException` (404) and `OrderDomainException` (400).
- All handlers return `ResponseEntity<ErrorDto>`. `ErrorDto` has `message` and `code` fields.

### HTTP responses (service_a)

- Controllers return `ResponseEntity<T>` directly.
- Success: `ResponseEntity.ok(body)` for 200, `ResponseEntity.status(HttpStatus.CREATED).body(body)` for 201.
- Errors: `ErrorDto(message, code)`.

### Infrastructure

- **Databases** (one per service):
  - `service_a_db` — PostgreSQL at `localhost:5432` (user/pass: `postgres/postgres`)
  - `service_b_db` — PostgreSQL at `localhost:5433` (user/pass: `postgres/postgres`)
- **Tables**:
  - `orders` → `OrderEntity` (service_a, order context)
  - `payments` → `OrderPaymentEntity` (service_a, order context, cascade from `orders`)
  - `processed_payments` → `PaymentEntity` (service_b, payment context)
  - `transactions` → `TransactionEntity` (service_b, payment context, cascade from `processed_payments`)
  - All primary keys are `UUID` — no `@GeneratedValue`.
- **Messaging**: Kafka at `localhost:9092`. Topics configured via `app.kafka.topics` in `KafkaTopicsConfig` (`@ConfigurationProperties` + `@Component`). Topics are auto-created on startup.
- **Schema**: managed by Hibernate `ddl-auto: update`.
