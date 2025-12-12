# ==========================================
# Stage 1: Build stage
# ==========================================
FROM gradle:8.5-jdk21-alpine AS build

WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradle gradle
COPY gradlew .
COPY gradlew.bat .
COPY settings.gradle.kts .
COPY build.gradle.kts .

# Download dependencies (cache layer)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src

# Build application (skip tests for faster build)
RUN ./gradlew build -x test --no-daemon

# ==========================================
# Stage 2: Runtime stage
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy built JAR from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/ || exit 1

# Expose port
EXPOSE 8080

# Run application with optimal JVM settings
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", \
  "app.jar"]