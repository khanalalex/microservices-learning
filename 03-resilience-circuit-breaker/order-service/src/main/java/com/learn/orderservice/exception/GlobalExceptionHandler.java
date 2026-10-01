package com.learn.orderservice.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 / 409 / 503 thrown by ProductClient and OrderService
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleStatus(ResponseStatusException ex) {
        return build(ex.getStatusCode().value(), ex.getReason());
    }

    // Product-service unreachable outside the circuit breaker
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Map<String, Object>> handleServiceDown(ResourceAccessException ex) {
        return build(503, "Product service is unavailable. Please try again later.");
    }

    // Invalid request body (for example quantity 0)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return build(400, msg);
    }

    private ResponseEntity<Map<String, Object>> build(int status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "status", status,
                "message", message == null ? "" : message,
                "timestamp", LocalDateTime.now().toString()));
    }
}
