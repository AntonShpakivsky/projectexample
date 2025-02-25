FROM eclipse-temurin:21-jre

WORKDIR /app

COPY build/libs/project-example.jar app.jar

CMD ["java", "-jar", "app.jar"]
