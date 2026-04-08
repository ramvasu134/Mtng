# ═══════════════════════════════════════════════════════════════════════════════
# Dockerfile – Multi-stage build for Mtng Spring Boot + React application
# ═══════════════════════════════════════════════════════════════════════════════
# Stage 1: Build the JAR (Maven + Node for React frontend)
# Stage 2: Slim JRE runtime image
# ═══════════════════════════════════════════════════════════════════════════════

# ── Stage 1: Build ───────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml ./

# Download dependencies (cache layer – only re-runs when pom.xml changes)
RUN mvn dependency:resolve -B -q 2>/dev/null || true
RUN mvn dependency:resolve-plugins -B -q 2>/dev/null || true

# Copy full source (frontend + backend)
COPY src ./src

# Build the application (frontend-maven-plugin builds React, then Maven packages the JAR)
RUN mvn clean package -DskipTests -B \
    && mv target/Mtng-*.jar target/app.jar

# ── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Install curl for health checks
RUN apk add --no-cache curl

# Create non-root user for security
RUN addgroup -S mtng && adduser -S mtng -G mtng -h /app -s /sbin/nologin

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/app.jar app.jar

# Copy the SSL keystore (needed for local profile; ignored by Render profile)
COPY --from=builder /app/src/main/resources/mtng-ssl.p12 mtng-ssl.p12

# Set ownership
RUN chown -R mtng:mtng /app

USER mtng

# Render injects PORT env-var (default 10000). Expose it.
EXPOSE ${PORT:-10000}

# ── Health check ─────────────────────────────────────────────────────────────
HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=90s \
  CMD curl -sf http://localhost:${PORT:-10000}/login || exit 1

# ── Entrypoint ───────────────────────────────────────────────────────────────
# • Render profile: SPRING_PROFILES_ACTIVE=render (set in Render env-vars)
# • JVM tuning for container memory awareness
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
