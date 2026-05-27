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
order/    — order management (create, get); also owns payment data persistence
shared/    — shared domain (Money value object, DomainException, ErrorDto)
```

There is no separate `payments/` bounded context. Payment data (`PaymentEntity`) lives inside `order/infrastructure/persistence/`. There is no `JpaPaymentRepository` — payments are persisted via JPA cascade from `OrderEntity`.

### Layer structure

```
{context}/
  domain/
    exception/     → DomainException hierarchy + error codes as string constants
    *.java         → domain model (immutable records) + port interfaces (no Spring, no JPA)
  aplication/      → service classes (e.g. OrderCreator, OrderFinder). No use case interfaces.
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

- **Driving ports** (input) — controllers depend directly on service classes in `aplication/` (no use case interfaces).
- **Driven ports** (output) — repository and event publisher interfaces in `domain/`. Services depend on these; adapters implement them.

```
Controller → ServiceClass → OrderRepository (interface) ← OrderPostgreSqlRepository → JpaOrderRepository
                          → OrderEventPublisher (interface) ← OrderKafkaEventPublisher → KafkaTemplate
```

### Order creation flow

`OrderDtoMapper.toDomain()` calls `Order.create(name, amount)` — the domain factory method that generates UUIDs for both `Order` and `Payment`, and sets `Payment.State.PENDING`. The resulting `Order` is passed directly to `OrderCreator`.

`OrderCreator.create()` is `@Transactional` and orchestrates the following steps atomically:
1. Saves the order via `OrderRepository` (JPA cascade saves the payment automatically)
2. Publishes `OrderCreatedEvent` to Kafka topic `amazon.env.order-management.orders.pub`

If any step fails, the transaction rolls back — neither the order nor the payment persist.

`OrderCreatedEvent` carries `type`, `orderId`, `amount`, and `paymentId`. ServiceB consumes this event, processes the payment, and is expected to publish a `PaymentCompletedEvent` back. ServiceA will consume that event to update the payment state (not yet implemented).

### Payment enrichment on reads

`OrderEntity` has a `@OneToOne(fetch = FetchType.EAGER) @JoinColumn(name = "payment_id")` relationship to `PaymentEntity`. JPA loads the payment automatically on every read via a JOIN. `OrderEntityMapper.toDomain()` converts the full `PaymentEntity` to a `Payment` domain object via `PaymentEntityMapper` — no manual enrichment needed in the adapter.

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
Order orderWithPayment = Order.create(order.name(), order.amount());
Order created = orderRepository.create(orderWithPayment);

orderEventPublisher.publishOrderCreated(created);

return created;
```

### Domain rules

- **Domain objects are immutable records** — `Order`, `Payment`, `Money` are Java records. Validation happens in the compact constructor.
- **UUIDs generated in domain** — `Order.create()` generates `UUID.randomUUID()` for both `Order.id` and `Payment.id`. No `@GeneratedValue` in JPA entities.
- **`Order.create(String name, Money amount)`** — static factory method. Creates an `Order` with a linked `Payment` in state `PENDING`. Use this for new orders; use the canonical constructor only for reconstructing from persistence.
- **`Money`** lives in `shared/domain/vo/`. Value object shared across contexts. Validates minimum amount in compact constructor — do not duplicate this validation in DTOs.
- **`order/domain/Payment`** is `order`'s own view of a payment (id + state). It is not a separate bounded context.
- **`Payment.State`** — `PENDING` (initial), `PAID`.
- **HTTP mappers** (`OrderDtoMapper`) are `@Component` beans injected by constructor — never static.
- **Persistence mappers** (`OrderEntityMapper`, `PaymentEntityMapper`) are `@Component` beans injected by constructor — never static.
- **DTOs** live in `infrastructure/http/dto/`. Domain objects are never serialized directly as API responses.
- **Domain exceptions** follow a hierarchy: `DomainException` (shared) → `OrderDomainException` → concrete exceptions. Each concrete exception defines its own error code as a string constant passed to the constructor. `DomainException.getCode()` exposes it.
- **Variable names must match the class name** in camelCase (e.g. `OrderCreator orderCreator`, `JpaOrderRepository jpaOrderRepository`).
- **Port naming** — domain interfaces have no suffix (e.g. `OrderRepository`, `OrderEventPublisher`).
- **Adapter naming** — adapters include the technology in the name (e.g. `OrderPostgreSqlRepository`, `OrderKafkaEventPublisher`).

### Exception handling

- `GlobalExceptionHandler` (shared/infrastructure) — handles `MethodArgumentNotValidException` and generic `Exception` only.
- `OrderExceptionHandler` (order/infrastructure/http) — handles `OrderNotFoundException` and `OrderDomainException`.
- All handlers return `ResponseEntity<ErrorDto>`. `ErrorDto` has `message` and `code` fields.

### HTTP responses

- Controllers return `ResponseEntity<T>` directly — no `ApiResponse` wrapper.
- Error responses use `ErrorDto(message, code)`.
- Success: `ResponseEntity.ok(body)` for 200, `ResponseEntity.status(HttpStatus.CREATED).body(body)` for 201.

### Infrastructure

- **Database**: PostgreSQL at `localhost:5432/amazon_db` (user/pass: `postgres/postgres`). Schema managed by Hibernate DDL auto (`ddl-auto: update`).
- **Tables**: `orders` (`OrderEntity`), `payments` (`PaymentEntity`). `orders.payment_id` is a `@OneToOne @JoinColumn` — Hibernate manages the FK constraint automatically. Both use `UUID` as primary key type.
- **Messaging**: Kafka at `localhost:9092`. `OrderCreator` publishes `OrderCreatedEvent` (type, orderId, amount, paymentId) to topic `amazon.env.order-management.orders.pub`. Topic is auto-created on startup via `KafkaTopicConfig`. Consumer side (for `PaymentCompletedEvent`) is not yet implemented.
- **Docker Compose**: `docker compose up -d` starts both PostgreSQL and Kafka.
