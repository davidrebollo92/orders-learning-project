--liquibase formatted sql

--changeset david:007-drop-payments-table
ALTER TABLE orders DROP CONSTRAINT fk_orders_payment;
DROP TABLE payments;
