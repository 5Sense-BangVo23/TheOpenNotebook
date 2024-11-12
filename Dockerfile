FROM maven:latest AS build

WORKDIR /app

COPY pom.xml ./
COPY src ./src

RUN mvn dependency:go-offline

EXPOSE 8089
CMD ["mvn", "spring-boot:run"]
