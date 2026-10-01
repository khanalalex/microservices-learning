# Project 3: Resilience (Circuit Breaker, Retry) and Distributed Tracing

Extends Project 2. Order-service now survives a failing product-service, and a single request can be followed across services by its trace id.

## Architecture

    Client --> api-gateway (8080) --> order-service (8082) --> product-service (8081)
                                          |
                                circuit breaker + retry
                                around calls to product-service

    All services register with eureka-server (8761).

## What was added
| Feature | Where | Behavior |
|---------|-------|----------|
| Circuit breaker (Resilience4j) | order-service, ProductClient | Opens when 50% of the last 6 calls fail (minimum 4 calls). Stays OPEN for 10 s, then HALF-OPEN with 2 trial calls |
| Retry with backoff | ProductClient.getProduct | 3 attempts, 200 ms then 400 ms pause. Only for GET (idempotent) |
| No retry on stock reduction | ProductClient.reduceStock | It decrements stock, so repeating it could take stock twice |
| Fallback | ProductClient | Fails fast with 503 and a clear message (FAILED vs CIRCUIT OPEN) |
| Business errors ignored by the breaker | ResilienceConfig | 404 and 409 are correct answers, not failures |
| Distributed tracing (Micrometer + Brave) | all three services | Trace id propagates between services in HTTP headers |

## Concepts covered
- Circuit breaker states: CLOSED, OPEN, HALF-OPEN
- Retry vs circuit breaker, retry storms, idempotency
- Timeout budget: attempts x timeout + backoff must fit inside any outer time limit
- Trace id vs span id, propagation, sampling

## Problems found and fixed while building it
- Spring Cloud's circuit breaker wrapper ran the call on a separate thread, so the trace context was lost and each call to product-service started a new trace. Fixed by using Resilience4j's CircuitBreaker directly, which runs on the calling thread.
- RestClient.Builder is not auto-configured as a bean in this setup, so the RestClient is built manually with the ObservationRegistry attached.
- order-service did not return error messages by default. It now has its own exception handler with a consistent JSON format.

## Tech stack
Java 17, Spring Boot 4.0.8, Spring Cloud (Eureka, Gateway, LoadBalancer), Resilience4j, Micrometer Tracing (Brave), Spring Data JPA, H2, Maven

## How to run
Start each module in its own terminal, in this order, waiting for each to start:

    1. eureka-server
    2. product-service
    3. order-service
    4. api-gateway

Command in each folder:

    .\mvnw.cmd spring-boot:run

Wait about 30 seconds until all services show in http://localhost:8761.

## Try it
Create a product, then place orders through the gateway:

    POST http://localhost:8080/api/products
    {"name":"Monitor","price":15000,"stock":50}

    POST http://localhost:8080/api/orders
    {"productId":1,"quantity":1}

## See the circuit breaker work
1. Stop product-service.
2. Place orders repeatedly. The first 4 fail with "Call to product-service FAILED", then the responses change to "CIRCUIT OPEN" and return instantly.
3. Wait 10 s: the next call is a HALF-OPEN trial.
4. Restart product-service (data resets because H2 is in-memory), recreate the product, and orders succeed again.

## See the trace id
Request logging is enabled at DEBUG level for DispatcherServlet in product-service and order-service. Place one order and compare the long hex id in square brackets in both services' logs. The trace id is the same, the span ids differ.

## Known limitations
- In-memory H2: data resets on restart
- Writes across two services are not atomic (Saga pattern, Project 4)
- Tracing is only verified between order-service and product-service. Trace data is not exported to a tool like Zipkin
- Circuit breaker settings are in Java code rather than configuration files

## Next
Project 4 adds Kafka for asynchronous, event-driven communication and the Saga pattern.
