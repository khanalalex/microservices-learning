# Project 1: Basic REST Microservices (Product + Order)

Two independent Spring Boot services that communicate over REST.

## Architecture

    Client --> order-service (8082) --HTTP--> product-service (8081)
                   |                               |
               orders DB (H2)                 products DB (H2)

## Concepts covered
- Monolith vs microservices, database per service
- Inter-service communication with RestClient
- Connect/read timeouts to avoid cascading failures
- DTOs as API contracts (no shared classes between services)
- Global exception handling with consistent HTTP status codes (400, 404, 409, 503)
- Known limitation: no distributed transaction. If saving the order fails after stock is reduced, data becomes inconsistent (solved with the Saga pattern in a later project)

## Tech stack
Java 17, Spring Boot 4.0.8, Spring Web, Spring Data JPA, H2, Bean Validation, Actuator, Maven

## How to run
Start each service in its own terminal:

    cd product-service
    .\mvnw.cmd spring-boot:run

    cd order-service
    .\mvnw.cmd spring-boot:run

## API

| Service | Method | Endpoint | Description |
|---------|--------|----------|-------------|
| product-service | POST | /api/products | Create product |
| product-service | GET | /api/products and /api/products/{id} | List or get product |
| product-service | PUT | /api/products/{id}/reduce-stock?quantity=n | Reduce stock |
| order-service | POST | /api/orders | Place order (productId, quantity) |
| order-service | GET | /api/orders and /api/orders/{id} | List or get orders |

## Example requests

    POST http://localhost:8081/api/products
    {"name":"Laptop","price":55000,"stock":10}

    POST http://localhost:8082/api/orders
    {"productId":1,"quantity":2}

## Next
Project 2 adds service discovery (Eureka), API Gateway and OpenFeign.
