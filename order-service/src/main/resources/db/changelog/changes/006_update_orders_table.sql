--liquibase formatted sql

--changeset david:006-update-orders-table
ALTER TABLE orders ADD COLUMN product_id UUID NOT NULL;
ALTER TABLE orders ADD COLUMN quantity INT NOT NULL;
ALTER TABLE orders DROP COLUMN name;