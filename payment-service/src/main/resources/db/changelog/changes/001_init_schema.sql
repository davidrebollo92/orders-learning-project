--liquibase formatted sql

--changeset david:001-create-transactions-table
CREATE TABLE transactions (
    id     UUID           NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    CONSTRAINT pk_transactions PRIMARY KEY (id)
);

--changeset david:001-create-payments-table
CREATE TABLE payments (
    id             UUID        NOT NULL,
    order_id       UUID        NOT NULL,
    state          VARCHAR(10) NOT NULL,
    transaction_id UUID,
    CONSTRAINT pk_payments          PRIMARY KEY (id),
    CONSTRAINT fk_payments_transaction FOREIGN KEY (transaction_id) REFERENCES transactions (id)
);

--changeset david:001-create-outbox-events-table
CREATE TABLE outbox_events (
    id           UUID         NOT NULL,
    aggregate_id UUID         NOT NULL,
    topic        VARCHAR(255) NOT NULL,
    payload      TEXT         NOT NULL,
    occurred_at  TIMESTAMPTZ  NOT NULL,
    published_at TIMESTAMPTZ,
    CONSTRAINT pk_outbox_events PRIMARY KEY (id)
);
