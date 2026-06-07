--liquibase formatted sql

--changeset david:003-add-event-type-to-outbox
ALTER TABLE outbox_events ADD COLUMN event_type VARCHAR(255);
