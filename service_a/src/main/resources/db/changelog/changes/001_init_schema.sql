--liquibase formatted sql

--changeset david:001-create-payments-table
CREATE TABLE payments (
    id    UUID        NOT NULL,
    state VARCHAR(10) NOT NULL,
    CONSTRAINT pk_payments PRIMARY KEY (id)
);

--changeset david:001-create-orders-table
CREATE TABLE orders (
    id         UUID           NOT NULL,
    name       VARCHAR(255)   NOT NULL,
    amount     NUMERIC(19, 2) NOT NULL,
    payment_id UUID,
    CONSTRAINT pk_orders     PRIMARY KEY (id),
    CONSTRAINT fk_orders_payment FOREIGN KEY (payment_id) REFERENCES payments (id)
);

--changeset david:001-create-outbox-events-table
CREATE TABLE outbox_events (
    id           UUID        NOT NULL,
    aggregate_id UUID        NOT NULL,
    topic        VARCHAR(255) NOT NULL,
    payload      TEXT        NOT NULL,
    occurred_at  TIMESTAMPTZ NOT NULL,
    published_at TIMESTAMPTZ,
    CONSTRAINT pk_outbox_events PRIMARY KEY (id)
);
