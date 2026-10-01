package com.learn.orderservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@Configuration
public class ResilienceConfig {

    // Resilience4j's own circuit breaker: it runs the call on the CALLING thread,
    // so the trace context (trace id) stays available for the outgoing HTTP request.
    @Bean
    public CircuitBreaker productCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(6)
                .minimumNumberOfCalls(4)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .permittedNumberOfCallsInHalfOpenState(2)
                .ignoreExceptions(ResponseStatusException.class) // 404/409 are not failures
                .build();
        return CircuitBreaker.of("productService", config);
    }
}
