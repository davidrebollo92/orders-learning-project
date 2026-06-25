# amazon

Proyecto de aprendizaje que implementa **Arquitectura Hexagonal** (Puertos y Adaptadores) con **DDD**, comunicación asíncrona vía **Kafka** y el patrón **Transactional Outbox**.

Proyecto Maven multi-módulo con tres módulos (Java 21 / Spring Boot 4.0.6):

- **shared** — librería compartida (`Money`, `DomainException`, `KafkaTopicsConfig`)
- **order-service** — gestión de pedidos (API REST + consumidor de eventos de pago)
- **payment-service** — procesamiento de pagos (consumidor/productor de eventos, sin HTTP)

## Patrones implementados

- **Hexagonal Architecture** — dominio aislado de infraestructura mediante puertos e interfaces
- **DDD** — agregados, value objects, excepciones de dominio, factorías en el dominio
- **Transactional Outbox** — los eventos se serializan como Avro binario y se guardan en base de datos (`BYTEA`) dentro de la misma transacción del negocio; un scheduler los publica a Kafka, garantizando consistencia sin two-phase commit
- **Saga con compensación** — flujo distribuido coordinado por eventos; si el pago falla, payment-service publica `PaymentFailedEvent` y order-service cancela la orden (`CANCELLED`)
- **Avro + Schema Registry** — los eventos Kafka se serializan con Apache Avro; los schemas (`.avsc`) son la fuente de verdad y se registran en Confluent Schema Registry; un topic por tipo de evento
- **Idempotencia** — los consumidores detectan y descartan eventos duplicados
- **Dead Letter Topic con persistencia** — tras agotar los reintentos, el evento se persiste en `dead_letter_events` (topic, payload Avro binario, tipo de evento, partición y offset originales) para su inspección y reprocesado manual
- **Migraciones con Liquibase** — el esquema de base de datos se gestiona mediante changelogs versionados; Hibernate solo valida, nunca altera
- **OpenAPI contract-first** — order-service expone su API REST a partir de un fichero `openapi.yaml`; el plugin `openapi-generator-maven-plugin` genera las interfaces Spring a partir de la spec en tiempo de compilación
- **AsyncAPI documentation** — cada servicio documenta su arquitectura de mensajería en un fichero `asyncapi.yaml` (AsyncAPI 3.0.0); describe los canales Kafka, las operaciones (`send`/`receive`) y los schemas de los mensajes desde la perspectiva de cada servicio; se sirve como recurso estático junto con una UI visual

## Flujo

**Happy path (importe ≤ 1000):**
1. `POST /orders` crea una orden (`CREATED`) con un pago (`PENDING`) y guarda `OrderCreatedEvent` serializado como Avro binario (`BYTEA`) en `outbox_events` de forma atómica
2. El `OutboxScheduler` de order-service deserializa el payload binario y publica el evento al topic `ordersCreated`
3. payment-service consume `OrderCreatedEvent`, cobra el pago vía `SimulatedPaymentGateway` y guarda `PaymentCompletedEvent` (Avro binario) en su `outbox_events`
4. El `OutboxScheduler` de payment-service publica el evento al topic `paymentsCompleted`
5. order-service consume `PaymentCompletedEvent` y transiciona la orden a `PAID`

**Flujo de compensación (importe > 1000):**
1–2. Igual que arriba
3. payment-service detecta fondos insuficientes, falla el pago y guarda `PaymentFailedEvent` (Avro binario) en su `outbox_events`
4. El `OutboxScheduler` de payment-service publica el evento al topic `paymentsFailed`
5. order-service consume `PaymentFailedEvent` y transiciona la orden a `CANCELLED`

## Gestión de errores Kafka

Los consumidores usan `@RetryableTopic` (3 reintentos, backoff exponencial). Las excepciones de negocio conocidas se absorben sin reintentar (duplicados → `warn`, no encontrado → `error`, fondos insuficientes → `warn`). Tras agotar los reintentos, el mensaje va al Dead Letter Topic y el `@DltHandler` persiste el evento fallido en la tabla `dead_letter_events` (topic, payload Avro, tipo de evento, partición y offset originales) para su inspección y reprocesado manual.

## Requisitos

- Docker

## Arrancar

```bash
docker compose up -d
```

Los servicios se construyen y arrancan automáticamente junto con la infraestructura (PostgreSQL × 2 + Kafka + Schema Registry).

## Endpoints (order-service)

```
POST   /orders       — crear pedido        → 201
GET    /orders       — listar pedidos      → 200
GET    /orders/{id}  — obtener por id      → 200 / 404
```

## Documentación de la API

### OpenAPI — order-service

| URL | Descripción |
|-----|-------------|
| `http://localhost:8080/swagger-ui.html` | Swagger UI (interfaz visual) |
| `http://localhost:8080/openapi/openapi.yaml` | Spec OpenAPI 3.0 en crudo |

### AsyncAPI

| URL | Descripción |
|-----|-------------|
| `http://localhost:8080/asyncapi/index.html` | UI visual AsyncAPI — order-service |
| `http://localhost:8080/asyncapi/asyncapi.yaml` | Spec AsyncAPI 3.0 en crudo — order-service |
| `http://localhost:8082/asyncapi/index.html` | UI visual AsyncAPI — payment-service |
| `http://localhost:8082/asyncapi/asyncapi.yaml` | Spec AsyncAPI 3.0 en crudo — payment-service |

Cada spec AsyncAPI describe, desde la perspectiva del servicio, qué topics publica (`action: send`) y cuáles consume (`action: receive`), con los schemas Avro de cada mensaje y los bindings Kafka (topic name, consumer group).

## Tests

```bash
# Desde la raíz — todos los tests unitarios y de capa web
./mvnw test -pl shared,order-service,payment-service

# Tests de integración (requieren Docker para Testcontainers + EmbeddedKafka)
./mvnw verify -pl order-service    # OrderIntegrationTest
./mvnw verify -pl payment-service  # PaymentIntegrationTest
```

Capas cubiertas: dominio, servicios de aplicación, capa web (`@WebMvcTest`), mappers HTTP y de persistencia, consumers Kafka (unitario), e integración end-to-end con Testcontainers (PostgreSQL) y EmbeddedKafka.
