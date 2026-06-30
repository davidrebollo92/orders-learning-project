FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY shared/pom.xml shared/pom.xml
COPY shared/src shared/src
COPY inventory-service/pom.xml inventory-service/pom.xml
COPY inventory-service/src inventory-service/src
RUN ./mvnw -pl shared,inventory-service -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/inventory-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]