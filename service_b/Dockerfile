FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY service_boot/pom.xml service_boot/pom.xml
COPY service_boot/src service_boot/src
COPY service_a/pom.xml service_a/pom.xml
COPY service_b/pom.xml service_b/pom.xml
COPY service_b/src service_b/src
RUN ./mvnw -pl service_boot,service_b -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/service_b/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
