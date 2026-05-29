# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./mvnw clean package

# Run
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=ServiceAApplicationTests

# Compile without running tests
./mvnw compile
```

Tests require a running PostgreSQL instance (see configuration below).

## Architecture

This is a **Spring Boot 4.0.6 / Java 21** service following **Hexagonal Architecture** (Ports and Adapters) with **DDD bounded contexts**.

### Bounded contexts

```
order/    — order management (create, get); owns the `payments` table via JPA cascade from `orders`
payment/  — payment processing; owns the `processed_payments` and `transactions` tables
shared/   — shared domain (Money value object, DomainException, ErrorDto, KafkaTopicsConfig)
```

Each bounded context has its own view of a payment:
- `order/domain/Payment` — lightweight view (id + state). Created by `Order.create()`.
- `payment/domain/Payment` — rich aggregate (id + state + Transaction). Processed by `PaymentProcessor`.

### Layer structure

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
Controller → ServiceClass → OrderRepository (interface) ← OrderPostgreSqlRepository → JpaOrderRepository
                          → OrderEventPublisher (interface) ← OrderKafkaEventPublisher → KafkaTemplate

KafkaConsumer → PaymentProcessor → PaymentRepository (interface) ← PaymentPostgreSqlRepository → JpaPaymentRepository
                                 → PaymentEventPublisher (interface) ← PaymentKafkaEventPublisher → KafkaTemplate
```

### Event-driven flow

```
POST /orders
  → OrderCreator (order/)
      → saves Order + Payment (PENDING) via JPA cascade → `orders` + `payments` tables
      → publishes OrderCreatedEvent → Kafka topic `orders`

OrderCreatedKafkaEventConsumer (payment/)
  → PaymentProcessor
      → idempotency check: if payment already in `processed_payments` → log warn, discard
      → creates Transaction, pays Payment
      → saves Payment (PAID) + Transaction → `processed_payments` + `transactions` tables
      → publishes PaymentCompletedEvent → Kafka topic `payments`

PaymentCompletedKafkaEventConsumer (order/)
  → PaymentCompleter
      → loads OrderPaymentEntity from `payments` table
      → if not found → log error, discard
      → if already PAID → log warn, discard
      → updates state to PAID in `payments` table
```

### Order creation flow (detail)

`OrderDtoMapper.toDomain()` calls `Order.create(name, amount)` — the domain factory method that generates UUIDs for both `Order` and `Payment`, and sets `Payment.State.PENDING`. The resulting `Order` is passed to `OrderCreator`.

`OrderCreator.create()` is `@Transactional` and orchestrates atomically:
1. Saves the order via `OrderRepository` (JPA cascade saves the payment automatically).
2. Publishes `OrderCreatedEvent` (type, orderId, amount, paymentId) to Kafka.

If any step fails, the transaction rolls back — neither the order nor the payment persist.

### Payment enrichment on reads

`OrderEntity` has a `@OneToOne(fetch = FetchType.EAGER) @JoinColumn(name = "payment_id")` relationship to `OrderPaymentEntity`. JPA loads the payment automatically on every read. `OrderEntityMapper.toDomain()` converts the full entity to a `Payment` domain object via `PaymentEntityMapper` — no manual enrichment needed in the adapter.

### Kafka event DTOs

Each context defines its own local event DTOs — no cross-context imports between bounded contexts:
- `order/infrastructure/messaging/` — `OrderCreatedEvent` (producer), `PaymentCompletedEvent` (consumer)
- `payment/infrastructure/messaging/` — `OrderCreatedEvent` (consumer, local copy), `PaymentCompletedEvent` (producer)

### Kafka error handling

Both Kafka consumers catch known domain exceptions and log them without retrying:
- Duplicate event (`PaymentAlreadyPaidException`, `PaymentAlreadyCompletedException`) → `log.warn`, discard.
- Payment not found (`PaymentNotFoundException`) → `log.error`, discard.
- Any other exception propagates to Kafka for retry.

### API endpoints

```
POST   /orders       → create order (returns 201)
GET    /orders       → list all orders (returns 200)
GET    /orders/{id}  → get order by id (returns 200)
```

### Code style

- **Blank lines between logical steps** — separate distinct steps within a method with a blank line. Group closely related lines together, separate different concerns.

```java
// correct
Order created = orderRepository.create(order);

orderEventPublisher.publishOrderCreated(created);

return created;
```

### Domain rules

- **Domain objects are immutable records** — `Order`, `Payment`, `Transaction`, `Money` are Java records.
- **UUIDs generated in domain** — `Order.create()` generates `UUID.randomUUID()` for both `Order.id` and `Payment.id`. `Transaction.create()` generates its own UUID. No `@GeneratedValue` in JPA entities.
- **`Order.create(String name, Money amount)`** — static factory method. Creates an `Order` with a linked `Payment` in state `PENDING`. Use this for new orders; use the canonical constructor only for reconstructing from persistence.
- **`payment/domain/Payment.create(UUID id, State state)`** — factory for reconstructing a payment from an external event. The compact constructor enforces that a `PAID` payment must always have a `Transaction`; violating this throws `InvalidPaymentStateException`.
- **`payment/domain/Payment.pay(Transaction)`** — behavior method that transitions state from `PENDING` to `PAID`. Throws `PaymentAlreadyPaidException` if already `PAID`.
- **`Money`** lives in `shared/domain/vo/`. Value object shared across contexts. `isBelowMinimum()` returns `boolean`. Do not duplicate minimum-amount validation in DTOs — `Order` validates it in its compact constructor.
- **HTTP mappers** (`OrderDtoMapper`) are `@Component` beans injected by constructor — never static.
- **Persistence mappers** (`OrderEntityMapper`, `PaymentEntityMapper`) are `@Component` beans injected by constructor — never static.
- **DTOs** live in `infrastructure/http/dto/`. Domain objects are never serialized directly as API responses.
- **Variable names must match the class name** in camelCase (e.g. `OrderCreator orderCreator`, `JpaOrderRepository jpaOrderRepository`).
- **Port naming** — domain interfaces have no suffix (e.g. `OrderRepository`, `PaymentEventPublisher`).
- **Adapter naming** — adapters include the technology in the name (e.g. `OrderPostgreSqlRepository`, `PaymentKafkaEventPublisher`).
- **`@Transactional`** — always use `org.springframework.transaction.annotation.Transactional`, never `jakarta.transaction.Transactional`.

### Exception hierarchies

```
DomainException (shared)
  └── OrderDomainException (order)
        ├── OrderNotFoundException
        ├── InvalidOrderAmountException
        ├── PaymentNotFoundException
        └── PaymentAlreadyCompletedException
  └── PaymentDomainException (payment)
        ├── PaymentAlreadyPaidException
        └── InvalidPaymentStateException
```

Each concrete exception defines its own error code as a string constant passed to the constructor. `DomainException.getCode()` exposes it.

### Exception handling

- `GlobalExceptionHandler` (shared/infrastructure) — handles `MethodArgumentNotValidException` and generic `Exception`.
- `OrderExceptionHandler` (order/infrastructure/http) — handles `OrderNotFoundException` (404) and `OrderDomainException` (400).
- All handlers return `ResponseEntity<ErrorDto>`. `ErrorDto` has `message` and `code` fields.
- `PaymentDomainException` has no HTTP handler — payment processing is event-driven only (no HTTP endpoints in the `payment` context).

### HTTP responses

- Controllers return `ResponseEntity<T>` directly — no `ApiResponse` wrapper.
- Error responses use `ErrorDto(message, code)`.
- Success: `ResponseEntity.ok(body)` for 200, `ResponseEntity.status(HttpStatus.CREATED).body(body)` for 201.

### Infrastructure

- **Database**: PostgreSQL at `localhost:5432/amazon_db` (user/pass: `postgres/postgres`). Schema managed by Hibernate DDL auto (`ddl-auto: update`).
- **Tables**:
  - `orders` → `OrderEntity` (order context)
  - `payments` → `OrderPaymentEntity` (order context, cascade from `orders` via `payment_id` FK)
  - `processed_payments` → `PaymentEntity` (payment context)
  - `transactions` → `TransactionEntity` (payment context, cascade from `processed_payments`)
  - All primary keys are `UUID` — no `@GeneratedValue`.
- **Messaging**: Kafka at `localhost:9092`. Topics configured via `app.kafka.topics.orders` and `app.kafka.topics.payments` in `KafkaTopicsConfig` (`@ConfigurationProperties`, registered via `@ConfigurationPropertiesScan` — no `@Component` needed). Topics are auto-created on startup via `OrderKafkaTopicConfig` and `PaymentKafkaTopicConfig`.
- **Docker Compose**: `docker compose up -d` starts both PostgreSQL and Kafka.
