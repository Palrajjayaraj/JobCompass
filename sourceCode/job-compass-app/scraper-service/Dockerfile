FROM maven:3.9.6-eclipse-temurin-21-jammy AS build
WORKDIR /app

# Copy parent POM and common module
COPY pom.xml .
COPY common/pom.xml common/pom.xml
COPY common/src common/src

# Copy service code
COPY scraper-service/pom.xml scraper-service/pom.xml
COPY scraper-service/src scraper-service/src
COPY storage-service/pom.xml storage-service/pom.xml

# Install parent POM, common module and build service
RUN mvn clean install -N
RUN mvn clean install -DskipTests -pl common
# Run tests for scraper-service (build will fail if tests fail)
RUN mvn test -pl scraper-service
# Package the service
RUN mvn clean package -DskipTests -pl scraper-service

# Switch to Playwright official image for runtime
# It contains JDK 17+ and all browser dependencies
FROM mcr.microsoft.com/playwright/java:v1.49.0-jammy

WORKDIR /app

# Copy the built JAR from Maven
COPY --from=build /app/scraper-service/target/*.jar app.jar

# Expose port 8082
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
