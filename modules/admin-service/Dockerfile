FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

COPY . .

RUN mvn -pl modules/admin-service -am dependency:go-offline
RUN mvn -pl modules/admin-service -am package -DskipTests

FROM eclipse-temurin:21-jdk-alpine AS runtime

WORKDIR /app

COPY --from=build /app/modules/admin-service/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]

EXPOSE 8084
