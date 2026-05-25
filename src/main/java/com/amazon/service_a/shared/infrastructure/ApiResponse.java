package com.amazon.service_a.shared.infrastructure;

import java.time.LocalDateTime;

// TODO Esta pieza es redundante. No aporta ningun valor extra.
// TODO para los errores tener un DTO -> ErrorDto
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        LocalDateTime timestamp
) {
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now());
    }
}
