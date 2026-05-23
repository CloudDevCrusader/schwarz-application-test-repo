# Multi-stage Dockerfile for Ktor application with SDKMAN

# Stage 1: Build stage
FROM ubuntu:22.04 AS builder

# Set environment variables
ENV DEBIAN_FRONTEND=noninteractive \
    SDKMAN_DIR=/root/.sdkman \
    GRADLE_USER_HOME=/root/.gradle

# Install dependencies
RUN apt-get update && apt-get install -y \
    curl \
    zip \
    unzip \
    git \
    && rm -rf /var/lib/apt/lists/*

# Install SDKMAN
RUN curl -s "https://get.sdkman.io" | bash

# Install Java and Kotlin via SDKMAN
RUN bash -c "source $SDKMAN_DIR/bin/sdkman-init.sh && \
    sdk install java 25.0.3-tem && \
    sdk install kotlin && \
    sdk install gradle"

# Set working directory
WORKDIR /app

# Copy gradle wrapper and build files
COPY gradlew gradlew.bat ./
COPY gradle gradle/
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY .sdkmanrc ./

# Copy source code
COPY core core/
COPY server server/
COPY client client/

# Build the application
RUN bash -c "source $SDKMAN_DIR/bin/sdkman-init.sh && \
    sdk use java 25.0.3-tem && \
    ./gradlew :server:build -x test --no-daemon"

# Stage 2: Runtime stage
FROM ubuntu:22.04

# Set environment variables
ENV DEBIAN_FRONTEND=noninteractive \
    SDKMAN_DIR=/root/.sdkman \
    JAVA_HOME=/root/.sdkman/candidates/java/current \
    PATH=/root/.sdkman/candidates/java/current/bin:$PATH

# Install dependencies and SDKMAN
RUN apt-get update && apt-get install -y \
    curl \
    zip \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Install SDKMAN and Java
RUN curl -s "https://get.sdkman.io" | bash && \
    bash -c "source $SDKMAN_DIR/bin/sdkman-init.sh && \
    sdk install java 25.0.3-tem"

# Create application directory
WORKDIR /app

# Copy built application from builder stage
COPY --from=builder /app/server/build/libs/*-all.jar /app/application.jar

# Copy application configuration template
COPY --from=builder /app/server/src/main/resources/application.yaml /app/application.yaml.template

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# Run the application
ENTRYPOINT ["/bin/bash", "-c", "source $SDKMAN_DIR/bin/sdkman-init.sh && sdk use java 25.0.3-tem && java -jar /app/application.jar"]
