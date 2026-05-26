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

There is no separate `payments/` bounded context. Payment data (`PaymentEntity`, `JpaPaymentRepository`) lives inside `order/infrastructure/persistence/`.

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
      mapper/      → persistence mappers (static)
      *.java       → JPA entities, JPA repositories, repository adapter
    messaging/     → Kafka adapters and event DTOs
```

Note: `aplication` is a pre-existing typo — do not rename it.

### Ports and adapters pattern

- **Driving ports** (input) — controllers depend directly on service classes in `aplication/` (no use case interfaces).
- **Driven ports** (output) — repository and event publisher ports in `domain/`. Services depend on these; adapters implement them.

```
Controller → ServiceClass → RepositoryPort (interface) ← RepositoryAdapter → JpaRepository
                          → EventPublisherPort (interface) ← EventPublisherAdapter → KafkaTemplate
```

### Order creation flow

`OrderCreator.create()` is `@Transactional` and orchestrates the following steps atomically:
1. Creates a `Payment` with state `CREATED` via `PaymentRepositoryPort`
2. Builds the `Order` with that payment linked
3. Saves the order via `OrderRepositoryPort`
4. Publishes `OrderCreatedEvent` to Kafka topic `orders.created`

If any step fails, the transaction rolls back — neither the order nor the payment persist.

`OrderCreatedEvent` carries `orderId`, `amount`, and `paymentId`. ServiceB consumes this event, processes the payment, and is expected to publish a `PaymentCompletedEvent` back. ServiceA will consume that event to update the payment state (not yet implemented).

### Payment enrichment on reads

`OrderEntity` has a `@OneToOne(fetch = FetchType.EAGER) @JoinColumn(name = "payment_id")` relationship to `PaymentEntity`. JPA loads the payment automatically on every read via a JOIN. `OrderMapper.toDomain()` converts the full `PaymentEntity` to a `Payment` domain object via `PaymentMapper` — no manual enrichment needed in the adapter.

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
Payment payment = paymentRepositoryPort.create(new Payment(null, Payment.State.CREATED));

Order orderWithPayment = new Order(order.id(), order.name(), order.amount(), payment);
Order created = orderRepositoryPort.create(orderWithPayment);

eventPublisherPort.publishOrderCreated(created);

return created;
```

### Domain rules

- **Domain objects are immutable records** — `Order`, `Payment`, `Money` are Java records. Validation happens in the compact constructor.
- **`Money`** lives in `shared/domain/vo/`. Value object shared across contexts.
- **`order/domain/Payment`** is `order`'s own view of a payment (id + state). It is not a separate bounded context.
- **HTTP mappers** (`OrderDtoMapper`) are `@Component` beans injected by constructor — never static.
- **Persistence mappers** (`OrderMapper`, `PaymentMapper`) are static utility classes in `persistence/mapper/`.
- **DTOs** live in `infrastructure/http/dto/`. Domain objects are never serialized directly as API responses.
- **Domain exceptions** follow a hierarchy: `DomainException` (shared) → `OrderDomainException` → concrete exceptions. Each concrete exception defines its own error code as a string constant passed to the constructor. `DomainException.getCode()` exposes it.
- **Variable names must match the class name** in camelCase (e.g. `OrderCreator orderCreator`, `JpaOrderRepository jpaOrderRepository`).
- **Adapter naming** — adapters are named after the domain concept, not the technology (e.g. `OrderEventPublisherAdapter`, not `KafkaOrderEventPublisherAdapter`).

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
- **Tables**: `orders` (`OrderEntity`), `payments` (`PaymentEntity`). `orders.payment_id` is a `@OneToOne @JoinColumn` — Hibernate manages the FK constraint automatically.
- **Messaging**: Kafka at `localhost:9092`. `OrderCreator` publishes `OrderCreatedEvent` (orderId, amount, paymentId) to topic `orders.created`. Consumer side (for `PaymentCompletedEvent`) is not yet implemented.
- **Docker Compose**: `docker compose up -d` starts both PostgreSQL and Kafka.
- Both `JpaOrderRepository` and `JpaPaymentRepository` use `GenerationType.IDENTITY`.
