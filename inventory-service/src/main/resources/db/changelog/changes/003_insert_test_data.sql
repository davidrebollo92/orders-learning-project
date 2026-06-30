--liquibase formatted sql

--changeset david:003-insert-test-products
INSERT INTO products (id, name, price, total_stock, reserved_stock)
VALUES ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Laptop', 999.99, 50, 0),
       ('b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Mouse', 29.99, 200, 0),
       ('c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'Keyboard', 79.99, 100, 0);