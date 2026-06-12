--liquibase formatted sql

--changeset david:005-create-dead-letter-events-table
CREATE TABLE dead_letter_events (
    id                 UUID         NOT NULL,
    topic              VARCHAR(255) NOT NULL,
    payload            BYTEA        NOT NULL,
    event_type         VARCHAR(255) NOT NULL,
    exception_message  TEXT,
    original_partition INT,
    original_offset    BIGINT,
    occurred_at        TIMESTAMPTZ  NOT NULL,
    CONSTRAINT pk_dead_letter_events PRIMARY KEY (id)
);
