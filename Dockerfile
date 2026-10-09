FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM icr.io/appcafe/open-liberty:full-java17-openj9-ubi
COPY --from=build /app/target/DreamHouse-1.0-SNAPSHOT.war /config/dropins/ROOT.war
COPY src/main/liberty/server.xml /config/server.xml
EXPOSE 9080
