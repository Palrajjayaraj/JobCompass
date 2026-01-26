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
RUN mvn clean package -DskipTests -pl scraper-service

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/scraper-service/target/*.jar app.jar

# Install Chromium for Selenium (supports both ARM64 and AMD64)
RUN apt-get update && apt-get install -y \
    chromium-browser \
    chromium-chromedriver \
    && rm -rf /var/lib/apt/lists/*

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
