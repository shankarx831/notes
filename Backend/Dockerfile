# -------------------------
# Stage 1: Build with JDK 21 + system Maven (works reliably)
# -------------------------
FROM eclipse-temurin:21 AS build

WORKDIR /app

# Install Maven (Debian-based image: apt available)
RUN apt-get update && \
    apt-get install -y --no-install-recommends maven ca-certificates && \
    rm -rf /var/lib/apt/lists/*

# Copy project and build
COPY pom.xml .
COPY src ./src

# Build the JAR (skip tests on CI builds for speed if you must; prefer not)
RUN mvn -B clean package -DskipTests

# -------------------------
# Stage 2: Run the application on lightweight Temurin Alpine JDK
# -------------------------
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy the produced jar (wildcard to handle name)
COPY --from=build /app/target/*.jar /app/app.jar

# Allow binding to Render-injected PORT via server.port=${PORT:8080}
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java","-Xms128m","-Xmx384m","-jar","/app/app.jar"]
