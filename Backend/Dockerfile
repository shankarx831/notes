# Use official OpenJDK image
FROM eclipse-temurin:21-jdk-alpine

# Set working directory
WORKDIR /app

# Copy Maven files for dependency caching
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Install dependencies (optional, improves build caching)
RUN ./mvnw dependency:resolve

# Copy source code
COPY src ./src

# Build the app
RUN ./mvnw clean package -DskipTests

# Expose the port (Render uses $PORT)
ENV PORT 8080
EXPOSE 8080

# Start command
CMD ["sh", "-c", "java -Xms128m -Xmx384m -jar target/*.jar"]
