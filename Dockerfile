FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B clean verify

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/internship-application-tracker-0.0.1-SNAPSHOT.jar app.jar
USER 10001
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=65.0"
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "app.jar"]
