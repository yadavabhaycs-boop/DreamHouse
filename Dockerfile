FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests
FROM quay.io/wildfly/wildfly:latest-jdk17
COPY --from=build /app/target/DreamHouse-1.0-SNAPSHOT.war /opt/jboss/wildfly/standalone/deployments/DreamHouse.war
EXPOSE 8080
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-J-Xms64m", "-J-Xmx256m", "-J-XX:MaxMetaspaceSize=128m"]
