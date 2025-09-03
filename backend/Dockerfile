#FROM eclipse-temurin:17-jre
#COPY target/*.jar app.jar
#EXPOSE 8083
#ENTRYPOINT ["java","-jar","/app.jar"]

# Stage 1: Build application
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Copy maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build application (skip tests for production build)
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -g 1001 appuser && adduser -u 1001 -G appuser -s /bin/sh -D appuser

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership
RUN chown appuser:appuser app.jar

# Switch to non-root user
USER appuser

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "/app/app.jar"]