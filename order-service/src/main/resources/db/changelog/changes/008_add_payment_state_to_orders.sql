--liquibase formatted sql

--changeset david:008-add-payment-state-to-orders
ALTER TABLE orders ADD COLUMN payment_state VARCHAR(10) NOT NULL DEFAULT 'PENDING';
