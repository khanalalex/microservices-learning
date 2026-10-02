# ---- Stage 1: build the jar ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q package -DskipTests

# ---- Stage 2: small runtime image ----
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --uid 1001 appuser
COPY --from=build /build/target/*.jar app.jar
USER appuser
ENTRYPOINT ["java", "-jar", "app.jar"]
