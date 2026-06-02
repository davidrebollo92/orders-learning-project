# amazon

Proyecto de aprendizaje de **Arquitectura Hexagonal** (Puertos y Adaptadores) con **DDD** y comunicación asíncrona vía **Kafka**.

Proyecto Maven multi-módulo con tres módulos (Java 21 / Spring Boot 4.0.6):

- **service_boot** — librería compartida (`Money`, `DomainException`, `KafkaTopicsConfig`)
- **service_a** — gestión de pedidos (API REST + consumidor de eventos de pago)
- **service_b** — procesamiento de pagos (consumidor/productor de eventos, sin HTTP)

## Flujo

1. `POST /orders` crea una orden con un pago en estado `PENDING` de forma atómica
2. Se publica `OrderCreatedEvent` en Kafka → service_b lo consume
3. service_b procesa el pago, crea una transacción y publica `PaymentCompletedEvent`
4. service_a consume el evento y actualiza el pago a `PAID`

## Requisitos

- Docker

## Arrancar

```bash
# 1. Infraestructura (PostgreSQL × 2 + Kafka)
docker compose up -d

# 2. Instalar módulos en el repositorio local de Maven
#    (necesario la primera vez y tras cualquier cambio en service_boot)
./service_a/mvnw install -DskipTests

# 3. Arrancar cada servicio (en terminales separadas)
cd service_a && ./mvnw spring-boot:run
cd service_b && ./mvnw spring-boot:run
```

## Endpoints (service_a)

```
POST   /orders       — crear pedido
GET    /orders       — listar pedidos
GET    /orders/{id}  — obtener pedido por id
```
