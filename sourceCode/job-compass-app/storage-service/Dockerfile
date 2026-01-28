FROM maven:3.9.6-eclipse-temurin-21-jammy AS build
WORKDIR /app

# Copy parent POM and common module
COPY pom.xml .
COPY common/pom.xml common/pom.xml
COPY common/src common/src

# Copy service code
COPY storage-service/pom.xml storage-service/pom.xml
COPY storage-service/src storage-service/src
COPY scraper-service/pom.xml scraper-service/pom.xml

# Install parent POM, common module and build service
RUN mvn clean install -N
RUN mvn clean install -DskipTests -pl common
# Run tests for storage-service (build will fail if tests fail)
RUN mvn test -pl storage-service
# Package the service
RUN mvn clean package -DskipTests -pl storage-service

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/storage-service/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
