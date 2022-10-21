FROM gradle:7.5.1-jdk11-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

FROM amazoncorretto:11-alpine3.15
EXPOSE 8080
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/throttling-test-task-gradle.jar
ENTRYPOINT ["java","-jar","/usr/app/throttling-test-task-gradle.jar"]
