# -------------------------
# Stage 1: Build the Spring Boot JAR
# -------------------------
FROM maven:3.9.3-eclipse-temurin-21-alpine AS build

# Set working directory
WORKDIR /app

# Copy pom.xml first to leverage Docker layer caching
COPY pom.xml .

# Copy source code
COPY src ./src

# Build JAR (skip tests for faster build on Render)
RUN mvn clean package -DskipTests

# -------------------------
# Stage 2: Run the application
# -------------------------
FROM eclipse-temurin:21-jdk-alpine

# Set working directory
WORKDIR /app

# Copy JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port (Render injects PORT env variable)
ENV PORT=8080
EXPOSE 8080

# JVM memory tuning (fits Render Free Tier 512MB container)
ENTRYPOINT ["java", "-Xms128m", "-Xmx384m", "-jar", "app.jar"]
