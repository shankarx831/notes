# Stage 1: Build with Maven + Temurin JDK 21
FROM maven:3.9.3-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run on lightweight Temurin Alpine JDK
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Let Spring bind to Render's injected PORT (server.port=${PORT:8080} in app)
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java","-Xms128m","-Xmx384m","-jar","/app/app.jar"]
