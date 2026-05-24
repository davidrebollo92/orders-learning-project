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
orders/    — order management (create, get)
payments/  — payment querying (get only; payments are created externally)
money/     — Money value object shared across contexts
```

### Layer structure (identical in both contexts)

```
{context}/
  domain/
    exception/     → base domain exception (abstract) + concrete exceptions
    *.java         → domain model + port interfaces (no Spring, no JPA)
  aplication/      → use case interfaces (ports) + service that implements them
  infrastructure/
    dto/           → request/response records + static DtoMapper
    *.java         → REST controller, JPA entity, repository adapter, static mapper
```

Note: `aplication` is a pre-existing typo — do not rename it.

### Ports and adapters pattern

Each context has two types of ports, both defined as interfaces in `domain/`:

- **Driving ports** (input) — use case interfaces in `aplication/` (e.g. `GetOrderUseCase`). Controllers depend on these, never on the service class directly.
- **Driven ports** (output) — repository ports in `domain/` (e.g. `OrderRepositoryPort`, `PaymentRepositoryPort`). Services depend on these; adapters implement them.

```
Controller → UseCase (interface) ← Service → RepositoryPort (interface) ← RepositoryAdapter → JpaRepository
```

### Cross-context communication (orders → payments)

`orders` needs payment data to enrich order responses. This is handled without domain coupling:

- `orders/domain/PaymentRepositoryPort` — port defined by `orders`, returns `orders/domain/Payment` (a record with `id` and `state`)
- `orders/infrastructure/PaymentRepositoryAdapter` — implements the port by accessing `payments/infrastructure/JpaPaymentRepository` directly (infrastructure-to-infrastructure, acceptable in a monolith)
- `OrderService.getOrder()` fetches the order, then enriches it with payment data via the port if `order.getPayment()` is not null

The `orders/domain/Payment` record is a stub when loaded from DB (`state = null`). `OrderService` replaces it with the full object after calling `PaymentRepositoryPort`.

### Domain rules

- **`orders/domain/Payment`** (record) is `orders`' own view of a payment — separate from `payments/domain/Payment` (class with enum `State`). Same name, different bounded contexts, different meaning.
- **`Money`** is a value object (Java record). `setAmount()` on `Order` enforces the invariant that amount must be greater than zero; violations throw `InvalidOrderAmountException`.
- **Mappers are static utility classes** — `OrderMapper` maps `Order` ↔ `OrderEntity`; `PaymentMapper` maps `Payment` ↔ `PaymentEntity`. `OrderDtoMapper` and `PaymentDtoMapper` map between domain and HTTP DTOs. These are separate concerns.
- **DTOs** live in `infrastructure/dto/`. Domain objects are never serialized directly as API responses.
- **Domain exceptions** follow a hierarchy: abstract base (`OrderDomainException`, `PaymentDomainException`) extended by concrete exceptions. Infrastructure can catch the base to handle all domain errors from a context uniformly.

### Infrastructure

- **Database**: PostgreSQL at `localhost:5432/amazon_db` (user/pass: `postgres/postgres`)
- **Tables**: `orders` (`OrderEntity`), `payments` (`PaymentEntity`). `orders.payment_id` is a plain `@Column` (no JPA relationship) — each context manages its own persistence independently.
- **Messaging**: Kafka configured but not yet implemented.
- Both `JpaOrderRepository` and `JpaPaymentRepository` use `GenerationType.IDENTITY`.

### Naming note

`PaymentResponse` exists in both `orders/infrastructure/dto/` and `payments/infrastructure/dto/`. They are different classes in different packages — if ever needed in the same file, use the fully qualified name for one of them.
