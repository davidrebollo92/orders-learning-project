--liquibase formatted sql

--changeset david:005-add-created-at-to-reservations
ALTER TABLE reservations
    ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT now();
