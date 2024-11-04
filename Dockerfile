# AuthService/Dockerfile
FROM openjdk:17-jdk-alpine
VOLUME /tmp
COPY build/libs/authservice.jar app.jar
COPY src/main/resources/keystore/authservice_local.jks /app/keystore/authservice_local.jks
ENTRYPOINT ["java","-jar","/app.jar"]
