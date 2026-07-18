FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/mosqueethononapp.jar mosqueethononapp.jar
ENTRYPOINT ["java", "-jar", "mosqueethononapp.jar"]
EXPOSE 8080