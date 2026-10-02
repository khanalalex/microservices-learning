# Project 5: JWT Security and Docker

Takes the Project 3 system (Eureka, API Gateway, product-service, order-service with circuit breaker and tracing) and adds a login service, JWT-based security at the gateway, and a full Docker Compose setup.

## Architecture

    Client --login--> api-gateway (8080) --> auth-service  --> JWT
    Client --JWT----> api-gateway (8080) --> order-service --> product-service
                          |
                  verifies signature, expiry and role

    All services register with eureka-server. Inside Docker only the gateway
    (and the Eureka dashboard, for learning) is published to the host.

## Modules
| Module | Role |
|--------|------|
| auth-service | Checks username and password (BCrypt), issues a signed JWT with the user's roles |
| api-gateway | Validates the JWT on every request, enforces role rules, routes by path |
| eureka-server | Service registry |
| product-service | Products and stock |
| order-service | Orders, calls product-service with circuit breaker and retry |

## Access rules (enforced at the gateway)
| Request | Who |
|---------|-----|
| POST /auth/login | Anyone |
| POST /api/products | ADMIN |
| Other /api/products/** and /api/orders/** | USER (admin also has USER) |
| Everything else | Nobody (default deny) |

Demo users: user / user123 (USER), admin / admin123 (ADMIN, USER). They live in memory for the demo.

## Concepts covered
- Authentication vs authorization, 401 vs 403
- JWT structure (header, payload, signature): signed, not encrypted
- Stateless security: no sessions, the token is the proof
- BCrypt password hashing, same error for unknown user and wrong password
- Reactive resource server security on Spring Cloud Gateway
- Multi-stage Docker builds, non-root container user, .dockerignore
- Docker Compose networking: service names as hostnames, published ports, Spring profile "docker"
- Network isolation as a second layer: only the gateway is reachable from outside
- Secrets from the environment (JWT_SECRET) instead of baked into images

## Problems found and fixed while building it
- A wrong password returned 403 instead of 401 because Spring Security also protected its internal /error path. Fixed by permitting /error.
- Docker could not start because a locally running Eureka already used port 8761.

## Tech stack
Java 17, Spring Boot 4.0.8, Spring Security (OAuth2 resource server, Nimbus JWT), Spring Cloud (Eureka, Gateway, LoadBalancer), Resilience4j, Micrometer Tracing, Docker, Docker Compose, Maven

## Run with Docker
    docker compose build
    docker compose up -d

Wait about 60 seconds for everything to register (dashboard: http://localhost:8761), then:

    POST http://localhost:8080/auth/login
    {"username":"admin","password":"admin123"}

Send the returned accessToken as the header "Authorization: Bearer <token>".

Set your own secret before running anything real:

    $env:JWT_SECRET = "a-long-random-secret-of-at-least-32-characters"

## What was verified by hand (against the containers)
- No token and tampered token: 401
- USER creating a product: 403. ADMIN creating a product: 201
- USER placing and reading an order through the gateway: 201 and 200
- Direct access to product-service and order-service from the host: connection refused

## Known limitations
- HS256 with one shared secret: every verifier could also forge tokens. A real system would use RS256 (private key signs, public key verifies)
- The default JWT secret in the properties and compose file is for demos only
- Users are in memory, no database, no registration, no refresh tokens, no token revocation
- No TLS between client and gateway or between services
- The Eureka dashboard port is published for learning and would not be in production
- No Docker health checks: startup order relies on services retrying registration
- Orders still use the synchronous flow from Project 3. Kafka (Project 4) is not part of this system
- Data is stored in in-memory H2 and resets on restart

## Interview topics this project covers
HS256 vs RS256, access vs refresh tokens, where to validate tokens, why default deny, why not run containers as root, image vs container, how containers find each other.
