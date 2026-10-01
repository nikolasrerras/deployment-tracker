
# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy Maven configuration first for layer caching
COPY pom.xml .

# Download dependencies
RUN mvn -B -ntp dependency:go-offline

# Copy application source code
COPY src ./src

# Build executable JAR
# Tests run separately in CI with Testcontainers
RUN mvn -B -ntp -DskipTests package


# Stage 2: Run the application
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/deployment-tracker-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

USER 10001:10001

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
