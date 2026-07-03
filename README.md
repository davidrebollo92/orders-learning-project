# amazon

Proyecto de aprendizaje que implementa **Arquitectura Hexagonal** (Puertos y Adaptadores) con **DDD**, comunicación **síncrona (HTTP)** y **asíncrona (Kafka + Avro)**, el patrón **Transactional Outbox** y patrones de **resiliencia**.

Proyecto Maven multi-módulo con cuatro módulos (Java 21 / Spring Boot 4.0.6):

- **shared** — librería compartida (`Money`, `DomainException`, `ErrorDto`, `GlobalExceptionHandler`, `AvroUtils`, `KafkaTopicsConfig`) y los schemas Avro (`.avsc`)
- **order-service** — gestión de pedidos (API REST; reserva stock por HTTP; consumidor de eventos de pago)
- **payment-service** — procesamiento de pagos (consumidor/productor de eventos, sin HTTP)
- **inventory-service** — catálogo y reservas de stock (API REST; consumidor de eventos de pago)

## Patrones implementados

- **Hexagonal Architecture** — dominio aislado de infraestructura mediante puertos e interfaces
- **DDD** — agregados, value objects, excepciones de dominio, factorías en el dominio
- **Comunicación síncrona + asíncrona** — al crear una orden, order-service **reserva stock por HTTP** en inventory-service (feedback inmediato de stock); el resto del flujo (pago, confirmación/cancelación) va por eventos Kafka
- **Transactional Outbox** — los eventos se serializan como Avro binario y se guardan en base de datos (`BYTEA`) dentro de la misma transacción del negocio; un scheduler los publica a Kafka, garantizando consistencia sin two-phase commit
- **Saga con compensación** — flujo distribuido coordinado por eventos; si el pago falla, payment-service publica `PaymentFailedEvent`, order-service cancela la orden (`CANCELLED`) e inventory-service libera la reserva
- **Resiliencia (Resilience4j)** — las llamadas HTTP order → inventory usan **retry** (reintenta fallos transitorios) y **circuit breaker** (corta en seco si inventory está caído, respondiendo 503 rápido); los errores de negocio no se reintentan
- **Reconciliador de reservas (reaper)** — un job programado en inventory reconcilia reservas huérfanas (consulta el estado de la orden y libera o confirma según corresponda), evitando stock bloqueado y oversell
- **Bloqueo pesimista** — las mutaciones de stock serializan reservas/confirmaciones concurrentes (`SELECT ... FOR UPDATE`)
- **Avro + Schema Registry** — los eventos Kafka se serializan con Apache Avro; los schemas (`.avsc`) viven en `shared` y son la fuente de verdad; un topic por tipo de evento
- **Idempotencia** — los consumidores detectan y descartan eventos duplicados; constraints `UNIQUE(order_id)` en pagos y reservas como red de seguridad
- **Dead Letter Topic con persistencia** — tras agotar los reintentos, el evento se persiste en `dead_letter_events` para su inspección y reprocesado manual
- **Migraciones con Liquibase** — el esquema se gestiona con changelogs versionados; Hibernate solo valida, nunca altera
- **OpenAPI contract-first** — order-service e inventory-service generan sus interfaces Spring a partir de un `openapi.yaml`
- **AsyncAPI documentation** — order-service y payment-service documentan su mensajería en un `asyncapi.yaml` (AsyncAPI 3.0.0)

## Flujo

**Happy path (importe ≤ 1000):**
1. `POST /orders` valida el producto y **reserva stock** en inventory-service (HTTP), crea la orden (`CREATED`, pago `PENDING`) y guarda `OrderCreatedEvent` (Avro binario) en `outbox_events` de forma atómica
2. El `OutboxScheduler` de order-service publica el evento al topic `ordersCreated`
3. payment-service consume `OrderCreatedEvent`, cobra el pago vía `SimulatedPaymentGateway` y publica `PaymentCompletedEvent`
4. order-service consume `PaymentCompletedEvent` y transiciona la orden a `PAID`
5. inventory-service consume `PaymentCompletedEvent` y **confirma** la reserva (descuenta el stock real)

**Flujo de compensación (importe > 1000):**
1–2. Igual que arriba
3. payment-service detecta fondos insuficientes, falla el pago y publica `PaymentFailedEvent`
4. order-service consume `PaymentFailedEvent` y transiciona la orden a `CANCELLED`
5. inventory-service consume `PaymentFailedEvent` y **libera** la reserva (devuelve el stock)

**Si inventory-service no está disponible** al reservar, order-service reintenta y, si persiste, responde **503** (`InventoryUnavailableException`) sin crear la orden.

## Gestión de errores Kafka

Los consumidores usan `@RetryableTopic` (3 reintentos, backoff exponencial). Las excepciones de negocio conocidas se absorben sin reintentar. Tras agotar los reintentos, el mensaje va al Dead Letter Topic y el `@DltHandler` persiste el evento fallido en `dead_letter_events` (topic, payload Avro, tipo de evento, partición y offset originales) para su inspección y reprocesado manual.

## Requisitos

- Docker

## Arrancar

```bash
docker compose up -d
```

Los servicios se construyen y arrancan automáticamente junto con la infraestructura (PostgreSQL × 3 + Kafka + Schema Registry). inventory-service arranca con 3 productos de ejemplo.

## Endpoints

### order-service (`http://localhost:8080`)
```
POST   /orders       — crear pedido        → 201 / 409 (sin stock) / 503 (inventory caído)
GET    /orders       — listar pedidos      → 200
GET    /orders/{id}  — obtener por id      → 200 / 404
```

### inventory-service (`http://localhost:8083`)
```
POST   /products       — crear producto    → 201
GET    /products       — listar productos  → 200
GET    /products/{id}  — obtener por id    → 200 / 404
POST   /reservations   — reservar stock    → 201 / 409 (sin stock) / 404 (producto)
```

## Documentación de la API

### OpenAPI

| URL | Descripción |
|-----|-------------|
| `http://localhost:8080/swagger-ui.html` | Swagger UI — order-service |
| `http://localhost:8080/openapi/openapi.yaml` | Spec OpenAPI — order-service |
| `http://localhost:8083/swagger-ui.html` | Swagger UI — inventory-service |
| `http://localhost:8083/openapi/openapi.yaml` | Spec OpenAPI — inventory-service |

### AsyncAPI

| URL | Descripción |
|-----|-------------|
| `http://localhost:8080/asyncapi/index.html` | UI AsyncAPI — order-service |
| `http://localhost:8082/asyncapi/index.html` | UI AsyncAPI — payment-service |

## Tests

```bash
# Desde la raíz — todos los tests
./mvnw test

# Un módulo
./mvnw test -pl order-service
```

Capas cubiertas: dominio, servicios de aplicación, capa web (`@WebMvcTest`), mappers HTTP y de persistencia, consumers Kafka (unitario), e integración end-to-end con Testcontainers (PostgreSQL) y EmbeddedKafka. Los tests de integración requieren Docker.
