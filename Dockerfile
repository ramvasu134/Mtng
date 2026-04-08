# ─────────────────────────────────────────────
# Stage 1 – Build the JAR with Maven
# ─────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper and POM first (layer cache for dependencies)
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw

# Download dependencies (cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# Copy source and build (skip tests for faster image build)
COPY src src
RUN ./mvnw package -DskipTests -B

# ─────────────────────────────────────────────
# Stage 2 – Minimal runtime image
# ─────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create a non-root user for security
RUN addgroup -S mtng && adduser -S mtng -G mtng
USER mtng

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Render injects PORT at runtime; Railway and others use PORT too.
# The shell form lets us pick it up dynamically.
EXPOSE 8080

# Use shell form so $PORT (or fallback 8080) is expanded at container start time
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
