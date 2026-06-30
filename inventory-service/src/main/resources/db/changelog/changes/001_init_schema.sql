--liquibase formatted sql

--changeset david:001-create-products-table
CREATE TABLE products
(
    id             UUID           NOT NULL,
    name           VARCHAR(255)   NOT NULL,
    price          NUMERIC(19, 2) NOT NULL,
    total_stock    INT            NOT NULL,
    reserved_stock INT            NOT NULL,
    CONSTRAINT pk_products PRIMARY KEY (id)
);

--changeset david:001-create-reservations-table
CREATE TABLE reservations
(
    id         UUID        NOT NULL,
    order_id   UUID        NOT NULL,
    product_id UUID        NOT NULL,
    quantity   INT         NOT NULL,
    state      VARCHAR(20) NOT NULL,
    CONSTRAINT pk_reservations PRIMARY KEY (id),
    CONSTRAINT fk_reservations_product FOREIGN KEY (product_id) REFERENCES products (id)
);

--changeset david:001-create-outbox-events-table
CREATE TABLE outbox_events
(
    id           UUID         NOT NULL,
    aggregate_id UUID         NOT NULL,
    topic        VARCHAR(255) NOT NULL,
    payload      BYTEA        NOT NULL,
    event_type   VARCHAR(255) NOT NULL,
    occurred_at  TIMESTAMPTZ  NOT NULL,
    published_at TIMESTAMPTZ,
    CONSTRAINT pk_outbox_events PRIMARY KEY (id)
);