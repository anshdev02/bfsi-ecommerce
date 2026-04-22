# ── Stage 1: Build ─────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Cache dependencies layer separately for faster rebuilds
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build the application (skip tests — run separately in CI)
COPY src ./src
RUN mvn clean package -DskipTests -B

# ── Stage 2: Run ────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Non-root user for security (BFSI compliance)
RUN addgroup -S bfsi && adduser -S bfsi -G bfsi
USER bfsi

COPY --from=build /app/target/ecommerce-banking-1.0.0.jar app.jar

# Expose application port
EXPOSE 8080

# JVM tuning for AWS t2.micro (1 GB RAM free tier)
ENTRYPOINT ["java", \
  "-Xms256m", \
  "-Xmx512m", \
  "-XX:+UseG1GC", \
  "-XX:MaxGCPauseMillis=200", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
