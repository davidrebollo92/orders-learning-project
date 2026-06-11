-- liquibase formatted sql

-- changeset david:004_alter_outbox_payload_to_bytea
ALTER TABLE outbox_events ALTER COLUMN payload TYPE BYTEA USING convert_to(payload, 'UTF8');
