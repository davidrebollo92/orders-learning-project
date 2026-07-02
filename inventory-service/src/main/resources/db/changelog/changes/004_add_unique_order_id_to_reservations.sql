--liquibase formatted sql

--changeset david:004-add-unique-order-id-to-reservations
ALTER TABLE reservations
    ADD CONSTRAINT uq_reservations_order_id UNIQUE (order_id);
