--liquibase formatted sql

--changeset david:002-add-state-to-orders
ALTER TABLE orders ADD COLUMN state VARCHAR(10) NOT NULL DEFAULT 'CREATED';
