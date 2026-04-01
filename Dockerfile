# ── Build stage ───────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /build

# Copy Maven wrapper + POM first so dependency layer is cached
COPY mvnw mvnw.cmd ./
COPY .mvn .mvn
COPY pom.xml ./
RUN ./mvnw dependency:go-offline -q

# Copy source and build the fat-JAR (skip tests; run tests separately in CI)
COPY src ./src
RUN ./mvnw clean package -DskipTests -q

# ── Runtime stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
LABEL maintainer="Mtng Team"

# Create a non-root user for security
RUN addgroup -S mtng && adduser -S mtng -G mtng

WORKDIR /opt/mtng

# Copy the built JAR from the builder stage
COPY --from=builder /build/target/Mtng-*.jar app.jar

# Create log directory
RUN mkdir -p logs && chown -R mtng:mtng /opt/mtng

USER mtng

# Expose the default Spring Boot port
EXPOSE 8080

# Start with the production profile active
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
