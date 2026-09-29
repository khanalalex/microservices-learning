# Project 2: Service Discovery + API Gateway

Extends Project 1. Services no longer use hard-coded addresses. They register with Eureka and find each other by name, and clients enter through a single API Gateway.

## Architecture

    Client --> api-gateway (8080) --> order-service (8082) --> product-service (8081)
                    |                        |                        |
                    +---- all register with / look up in: eureka-server (8761)

## Modules
| Module | Port | Role |
|--------|------|------|
| eureka-server | 8761 | Service registry (phone book) |
| product-service | 8081 | Manages products and stock |
| order-service | 8082 | Places orders, calls product-service by name |
| api-gateway | 8080 | Single entry point, routes /api/products/** and /api/orders/** |

## Concepts covered
- Service discovery with Eureka (registration, heartbeats, self-preservation mode)
- Client-side load balancing: order-service calls http://product-service and resolves an instance from the registry
- API Gateway routing with predicates and lb:// URIs (Spring Cloud Gateway)
- Why services register by IP (eureka.instance.prefer-ip-address=true): the gateway's DNS resolver could not resolve the machine hostname
- Timeouts and 503 handling carried over from Project 1

## Tech stack
Java 17, Spring Boot 4.0.8, Spring Cloud (Netflix Eureka, Gateway, LoadBalancer), Spring Data JPA, H2, Maven

## How to run
Start each module in its own terminal, in this order, waiting for it to finish starting:

    1. eureka-server
    2. product-service
    3. order-service
    4. api-gateway

Command in each folder:

    .\mvnw.cmd spring-boot:run

Open http://localhost:8761 to see registered services.

## Try it (all through the gateway)

    POST http://localhost:8080/api/products
    {"name":"Tablet","price":20000,"stock":7}

    POST http://localhost:8080/api/orders
    {"productId":1,"quantity":3}

    GET http://localhost:8080/api/orders

## Known limitations
- Data is stored in in-memory H2 and resets on restart
- No circuit breaker or retry yet (Project 3)
- Writes across two services are not atomic (Saga pattern, Project 4)
- Configuration is per-service; a central Config Server is not included

## Next
Project 3 adds Resilience4j (circuit breaker, retry, fallback) and distributed tracing.
