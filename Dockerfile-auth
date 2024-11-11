FROM openjdk:21-jdk-alpine
VOLUME /tmp
COPY build/libs/forrrest-authservice.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]