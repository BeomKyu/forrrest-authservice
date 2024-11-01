# Stage 1: Build
FROM gradle:8.2.1-jdk-21 AS build
COPY --chown=gradle:gradle . /home/gradle/project
WORKDIR /home/gradle/project
RUN gradle build --no-daemon

# Stage 2: Run
FROM openjdk:21-jdk-alpine
VOLUME /tmp
COPY --from=build /home/gradle/project/build/libs/authservice-0.0.1-SNAPSHOT.jar authservice.jar
ENTRYPOINT ["java","-jar","/authservice.jar"]
