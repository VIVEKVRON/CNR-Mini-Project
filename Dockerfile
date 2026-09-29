# Build stage
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
# Copy the backend pom and source code
COPY backend/pom.xml .
COPY backend/src ./src
COPY backend/soil_model.onnx .
# Package the application
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar
COPY backend/soil_model.onnx .
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
