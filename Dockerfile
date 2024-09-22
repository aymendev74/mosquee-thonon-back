FROM openjdk:18.0-slim
COPY target/mosqueethononapp.jar /app/mosqueethononapp.jar
ENTRYPOINT ["java", "-jar", "/app/mosqueethononapp.jar"]
EXPOSE 8080