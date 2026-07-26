FROM eclipse-temurin:17-jdk AS build

WORKDIR /build

# Copier uniquement ce qui sert à télécharger les dépendances
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .
COPY docs/functional docs/functional

RUN --mount=type=cache,target=/root/.m2 \
    chmod +x mvnw && ./mvnw dependency:go-offline

# Ensuite seulement le code
COPY src src

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -DskipTests package

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /build/target/mosqueethononapp.jar .
COPY --from=build /build/docs ./docs

ENTRYPOINT ["java","-jar","mosqueethononapp.jar"]