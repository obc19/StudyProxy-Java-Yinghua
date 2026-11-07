FROM openjdk:8-jre-alpine

WORKDIR /app

COPY target/StudyProxy-Java-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]