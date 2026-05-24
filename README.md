# orders-learning-project

Servicio Spring Boot (Java 21 / Spring Boot 4.0.6) para gestión de pedidos y pagos.

Proyecto de aprendizaje de **Arquitectura Hexagonal** (Puertos y Adaptadores) con **contextos acotados DDD**.

## Contextos

- **orders** — creación y consulta de pedidos
- **payments** — consulta de pagos (creados externamente)
- **money** — value object `Money` compartido

## Requisitos

- PostgreSQL en `localhost:5432/amazon_db` (usuario/contraseña: `postgres/postgres`)
- Levantar con Docker: `docker-compose up -d`

## Arrancar

```bash
./mvnw spring-boot:run
```
