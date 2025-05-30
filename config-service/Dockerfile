FROM eclipse-temurin:21-jdk-jammy


WORKDIR /app

COPY ./config-repo /app/config-repo


ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar


ENV SERVER_PORT=8887
EXPOSE ${SERVER_PORT}


ENTRYPOINT ["java", "-jar", "app.jar"]