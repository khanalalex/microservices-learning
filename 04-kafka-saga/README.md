# Project 4: Event-Driven Communication with Kafka and the Saga Pattern

Order-service and product-service no longer call each other over HTTP. They communicate through Kafka events, and a choreography-based saga keeps their data consistent without a distributed transaction.

## Flow

    POST /api/orders (order-service, 8082)
      1. save order as PENDING                          (local transaction)
      2. publish OrderCreatedEvent  --> topic order-events
                                                |
                                  product-service (8081) consumes it
      3. reserve stock + record outcome                 (local transaction)
      4. publish StockResultEvent   --> topic stock-events
                                                |
                                  order-service consumes it
      5. order becomes CONFIRMED (stock reserved) or CANCELLED (compensating action)

The API answers immediately with status PENDING. The final status is eventually consistent.

## Concepts covered
- Synchronous (REST) vs asynchronous (Kafka) communication
- Kafka basics: topics, partitions, producers, consumers, consumer groups, offsets
- Events as facts in the past tense (OrderCreated), not commands
- Saga pattern (choreography) and compensating actions
- Idempotent consumers: product-service stores one row per order id, so a duplicate event does not reduce stock twice
- Dual write problem and the Outbox pattern (described, not implemented)
- Events are exchanged as JSON strings, with each service owning its own copy of the event classes

## Tech stack
Java 17, Spring Boot 4.0.8, Spring for Apache Kafka, Apache Kafka 3.9 (KRaft mode, via Docker), Spring Data JPA, H2, Maven

## How to run
1. Start Kafka and create the topics:

        docker compose up -d
        docker exec kafka /opt/kafka/bin/kafka-topics.sh --create --topic order-events --partitions 3 --replication-factor 1 --bootstrap-server localhost:9092
        docker exec kafka /opt/kafka/bin/kafka-topics.sh --create --topic stock-events --partitions 3 --replication-factor 1 --bootstrap-server localhost:9092

2. Start each service in its own terminal:

        cd product-service
        .\mvnw.cmd spring-boot:run

        cd order-service
        .\mvnw.cmd spring-boot:run

## Try it

    POST http://localhost:8081/api/products
    {"name":"Monitor","price":15000,"stock":5}

    POST http://localhost:8082/api/orders
    {"productId":1,"quantity":2}
    -> status PENDING, then CONFIRMED a moment later (GET /api/orders/{id})

    POST http://localhost:8082/api/orders
    {"productId":1,"quantity":50}
    -> status CANCELLED, stock unchanged

## What was verified by hand
- Happy path: PENDING, then CONFIRMED, stock reduced
- Compensation: not enough stock gives CANCELLED and stock stays unchanged
- Order placed while product-service was down: the event waited in Kafka and was processed after product-service started

## Known limitations
- Idempotency is implemented but was not tested with real duplicate deliveries
- No Outbox: if order-service crashes between saving the order and publishing the event, the order stays PENDING
- No dead-letter topic or retry policy for events that fail to process
- Data is stored in in-memory H2 and resets on restart
- No API gateway or service discovery in this project, because the services only talk through Kafka

## Next
Project 5 adds security (JWT), containerizes the services with Docker and runs them together.
