FROM eclipse-temurin:17-jdk AS build

WORKDIR /build

# Copier uniquement ce qui sert à télécharger les dépendances
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .
COPY docs/functional docs/functional
COPY src src

RUN --mount=type=cache,target=/root/.m2 \
    chmod +x mvnw && ./mvnw -B -DskipTests clean package

FROM eclipse-temurin:17-jre AS runtime

WORKDIR /app

COPY --from=build /build/target/mosqueethononapp.jar .
COPY --from=build /build/docs ./docs

ENTRYPOINT ["java","-jar","mosqueethononapp.jar"]