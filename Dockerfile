FROM maven:3.9.12-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY src ./src
RUN mvn -B -DskipTests -Pprod clean package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/gestion-abonnements-1.0-SNAPSHOT.jar app.jar
COPY data ./data
EXPOSE 4567
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD java -jar /app/app.jar help > /dev/null 2>&1 || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
