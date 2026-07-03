package com.amazon.order_service.order.domain.exception;

public class InventoryUnavailableException extends OrderDomainException {
    public InventoryUnavailableException() {
        super("Inventory service is unavailable", "INVENTORY_UNAVAILABLE");
    }
}
