
FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/demo-0.0.1-SNAPSHOT.jar app.jar
COPY src/main/resources/Template.docx /app/src/main/resources/Template.docx

ENV CSV_FILE_PATH="/app/data/csvfile.csv"

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
