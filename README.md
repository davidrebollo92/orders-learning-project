# amazon

Proyecto de aprendizaje que implementa **Arquitectura Hexagonal** (Puertos y Adaptadores) con **DDD**, comunicación asíncrona vía **Kafka** y el patrón **Transactional Outbox**.

Proyecto Maven multi-módulo con tres módulos (Java 21 / Spring Boot 4.0.6):

- **service_boot** — librería compartida (`Money`, `DomainException`, `KafkaTopicsConfig`)
- **service_a** — gestión de pedidos (API REST + consumidor de eventos de pago)
- **service_b** — procesamiento de pagos (consumidor/productor de eventos, sin HTTP)

## Patrones implementados

- **Hexagonal Architecture** — dominio aislado de infraestructura mediante puertos e interfaces
- **DDD** — agregados, value objects, excepciones de dominio, factorías en el dominio
- **Transactional Outbox** — los eventos se guardan en base de datos dentro de la misma transacción del negocio; un scheduler los publica a Kafka, garantizando consistencia sin two-phase commit
- **Coreografía (Saga)** — flujo distribuido coordinado por eventos, sin orquestador central
- **Idempotencia** — los consumidores detectan y descartan eventos duplicados

## Flujo

1. `POST /orders` crea una orden con un pago en estado `PENDING` y guarda `OrderCreatedEvent` en la tabla `outbox_events` de forma atómica
2. El `OutboxScheduler` de service_a publica el evento a Kafka
3. service_b consume `OrderCreatedEvent`, procesa el pago, crea una transacción y guarda `PaymentCompletedEvent` en su `outbox_events`
4. El `OutboxScheduler` de service_b publica el evento a Kafka
5. service_a consume `PaymentCompletedEvent` y actualiza el pago a `PAID`

## Gestión de errores Kafka

Los consumidores usan `@RetryableTopic` (3 reintentos, backoff exponencial). Las excepciones de negocio conocidas se absorben sin reintentar (duplicados → `warn`, no encontrado → `error`). Tras agotar reintentos, el mensaje va al Dead Letter Topic.

## Requisitos

- Docker

## Arrancar

```bash
docker compose up -d
```

Los servicios se construyen y arrancan automáticamente junto con la infraestructura (PostgreSQL × 2 + Kafka).

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
