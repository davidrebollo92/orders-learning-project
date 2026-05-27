# service-A

Servicio Spring Boot (Java 21 / Spring Boot 4.0.6) para gestión de pedidos.

Proyecto de aprendizaje de **Arquitectura Hexagonal** (Puertos y Adaptadores) con **DDD**.

## Contextos

- **order** — creación y consulta de pedidos; gestiona el ciclo de vida del pago asociado
- **shared** — value objects y utilidades compartidas (`Money`, `DomainException`, `ErrorDto`)

## Flujo de creación de un pedido

1. `POST /orders` crea la orden y un pago en estado `PENDING` de forma atómica
2. Se publica un evento `OrderCreatedEvent` en Kafka (topic `orders.created`) con `orderId`, `amount` y `paymentId`
3. Un servicio externo (ServiceB) consume el evento, procesa el pago y publicará `PaymentCompletedEvent`
4. ServiceA consumirá ese evento para actualizar el estado del pago *(pendiente de implementar)*

## Requisitos

- PostgreSQL en `localhost:5432/amazon_db` (usuario/contraseña: `postgres/postgres`)
- Kafka en `localhost:9092`
- Levantar ambos con Docker: `docker compose up -d`

## Arrancar

```bash
./mvnw spring-boot:run
```

## Endpoints

```
POST   /orders       — crear pedido
GET    /orders       — listar pedidos
GET    /orders/{id}  — obtener pedido por id
```
