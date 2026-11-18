# Multi-stage Dockerfile for ForgeOps Spring Boot Backend
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw ./
COPY .mvn .mvn
COPY pom.xml ./

# Pre-fetch dependencies
RUN ./mvnw dependency:go-offline -B || true

# Copy source code and build production jar
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Runtime Stage
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Create non-root user for security
RUN groupadd -r forgeops && useradd -r -g forgeops forgeops

# Copy built artifact from build stage
COPY --from=build --chown=forgeops:forgeops /app/target/forgeops-backend-*.jar app.jar

USER forgeops

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
