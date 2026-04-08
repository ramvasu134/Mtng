# ── Stage 1: Build ────────────────────────────────────────────
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

# Copy Maven wrapper and config
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Make wrapper executable and download dependencies first (layer cache)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code (frontend + backend)
COPY src/ src/

# Build the fat JAR (frontend-maven-plugin builds React automatically)
RUN ./mvnw package -DskipTests -B

# ── Stage 2: Run ──────────────────────────────────────────────
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

# Render sets PORT env var; default to 8080 for local Docker usage.
# SSL is disabled because Render handles HTTPS termination at the load balancer.
ENV PORT=8080

EXPOSE 8080

ENTRYPOINT exec java -jar app.jar \
  --server.port=${PORT} \
  --server.ssl.enabled=false
