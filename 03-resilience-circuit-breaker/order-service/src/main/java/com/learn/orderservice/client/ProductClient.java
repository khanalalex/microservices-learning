package com.learn.orderservice.client;

import com.learn.orderservice.dto.ProductResponse;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.function.Supplier;

@Component
public class ProductClient {

    private static final Logger log = LoggerFactory.getLogger(ProductClient.class);
    private static final int MAX_ATTEMPTS = 3;

    private final RestClient restClient;
    private final CircuitBreaker circuitBreaker;

    public ProductClient(RestClient productRestClient, CircuitBreaker productCircuitBreaker) {
        this.restClient = productRestClient;
        this.circuitBreaker = productCircuitBreaker;
    }

    // Safe to repeat (GET), so retries happen inside the circuit breaker
    public ProductResponse getProduct(Long id) {
        return execute(() -> fetchProductWithRetry(id));
    }

    // NOT idempotent (it decrements stock), so it is never retried
    public void reduceStock(Long id, int quantity) {
        execute(() -> {
            sendReduceStock(id, quantity);
            return Boolean.TRUE;
        });
    }

    // Runs the call through the circuit breaker on the current thread and
    // converts failures into clear HTTP errors (the "fallback")
    private <T> T execute(Supplier<T> call) {
        try {
            return circuitBreaker.executeSupplier(call);
        } catch (ResponseStatusException e) {
            throw e; // business errors (404, 409) pass through unchanged
        } catch (CallNotPermittedException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "CIRCUIT OPEN: calls to product-service are being rejected without trying");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Call to product-service FAILED: " + e.getClass().getSimpleName());
        }
    }

    private ProductResponse fetchProductWithRetry(Long id) {
        ResourceAccessException lastError = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return fetchProduct(id);
            } catch (ResourceAccessException e) {   // network problem or timeout only
                lastError = e;
                log.warn("getProduct({}) attempt {}/{} failed: {}", id, attempt, MAX_ATTEMPTS,
                        e.getClass().getSimpleName());
                if (attempt < MAX_ATTEMPTS) {
                    pause(200L * attempt);           // backoff: 200 ms, then 400 ms
                }
            }
        }
        throw lastError;
    }

    private void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private ProductResponse fetchProduct(Long id) {
        return restClient.get()
                .uri("/api/products/{id}", id)
                .retrieve()
                .onStatus(status -> status.value() == 404, (req, res) -> {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Product " + id + " does not exist");
                })
                .body(ProductResponse.class);
    }

    private void sendReduceStock(Long id, int quantity) {
        restClient.put()
                .uri("/api/products/{id}/reduce-stock?quantity={q}", id, quantity)
                .retrieve()
                .onStatus(status -> status.value() == 409, (req, res) -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Insufficient stock for product " + id);
                })
                .toBodilessEntity();
    }
}
