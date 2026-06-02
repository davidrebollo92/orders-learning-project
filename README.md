# amazon

Proyecto de aprendizaje de **Arquitectura Hexagonal** (Puertos y Adaptadores) con **DDD** y comunicación asíncrona vía **Kafka**.

Monorepo con dos servicios Spring Boot (Java 21 / Spring Boot 4.0.6):

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
# Infraestructura (PostgreSQL × 2 + Kafka)
docker compose up -d

# Instalar módulos en el repositorio local (necesario la primera vez
# y cada vez que se modifique service_boot)
./service_a/mvnw install -DskipTests

# service_a (puerto 8080)
cd service_a && ./mvnw spring-boot:run

# service_b
cd service_b && ./mvnw spring-boot:run
```

## Endpoints (service_a)

```
POST   /orders       — crear pedido
GET    /orders       — listar pedidos
GET    /orders/{id}  — obtener pedido por id
```
