FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn -B -q dependency:resolve dependency:resolve-plugins

COPY src ./src
RUN mvn -B -q package -Dmaven.test.skip=true -Dspring.jpa.hibernate.ddl-auto=none

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/message-manager-*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
