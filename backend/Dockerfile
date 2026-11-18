# Multi-stage Dockerfile for ForgeOps Spring Boot Backend
FROM maven:3.9.9-eclipse-temurin-21-jammy AS build

WORKDIR /app

# Cache dependencies
COPY pom.xml ./
RUN mvn dependency:go-offline -B || true

# Copy source code and build production jar
COPY src ./src
RUN mvn clean package -DskipTests

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
