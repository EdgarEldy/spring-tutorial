# Stage 1: build the WAR with the full multi-module Maven reactor
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy every module's pom.xml first so Maven can resolve/cache dependencies
# before the source code is copied in (better layer caching on rebuilds).
COPY pom.xml .
COPY spring-tutorial-common/pom.xml spring-tutorial-common/pom.xml
COPY spring-tutorial-domain/pom.xml spring-tutorial-domain/pom.xml
COPY spring-tutorial-dao/pom.xml spring-tutorial-dao/pom.xml
COPY spring-tutorial-service/pom.xml spring-tutorial-service/pom.xml
COPY spring-tutorial-web/pom.xml spring-tutorial-web/pom.xml
RUN mvn -q -B dependency:go-offline || true

COPY . .
RUN mvn -q -B -T 1C clean package -DskipTests

# Stage 2: deploy the WAR on a plain Tomcat 10 (Jakarta EE 9+, matches the
# jakarta.servlet-api used in spring-tutorial-web)
FROM tomcat:10-jdk17
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /workspace/spring-tutorial-web/target/spring-tutorial-web.war \
    /usr/local/tomcat/webapps/spring-tutorial-web.war
EXPOSE 8080
CMD ["catalina.sh", "run"]
