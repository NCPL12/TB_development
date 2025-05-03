# Use OpenJDK 17 as base image
FROM maven:3.9.4-eclipse-temurin-17 as build

# Set working directory
WORKDIR /app

# Copy project files
COPY . .

# Build the Spring Boot app (skip tests for faster build)
RUN mvn clean package -DskipTests

# Use a smaller JDK image for runtime
FROM eclipse-temurin:17-jdk-alpine

# Set working directory in runtime image
WORKDIR /app

# Copy the built jar from the build image
COPY --from=build /app/target/bms-reports-0.0.1-SNAPSHOT.jar app.jar

# Set the command to run the JAR
CMD ["java", "-jar", "app.jar"]
