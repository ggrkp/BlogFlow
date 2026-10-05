FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

RUN apk add --no-cache maven

COPY pom.xml .

EXPOSE 8080

CMD ["mvn", "spring-boot:run"]