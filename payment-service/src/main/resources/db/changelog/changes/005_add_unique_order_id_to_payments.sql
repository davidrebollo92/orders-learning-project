--liquibase formatted sql

--changeset david:005-add-unique-order-id-to-payments
ALTER TABLE payments
    ADD CONSTRAINT uq_payments_order_id UNIQUE (order_id);
