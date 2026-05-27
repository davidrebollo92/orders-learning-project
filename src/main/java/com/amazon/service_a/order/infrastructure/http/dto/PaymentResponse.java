package com.amazon.service_a.order.infrastructure.http.dto;

import java.util.UUID;

public record PaymentResponse(UUID id, String state) {
}
