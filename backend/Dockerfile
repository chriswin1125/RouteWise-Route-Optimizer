# Stage 1: Build application
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
# Pre-fetch dependencies for caching
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Production runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Non-root user for security
RUN addgroup -S routewise && adduser -S routewise -G routewise
USER routewise:routewise

COPY --from=build /app/target/routewise-backend-1.0.0.jar app.jar

ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=postgres

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
