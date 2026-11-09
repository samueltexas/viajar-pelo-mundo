FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

COPY .mvn .mvn
COPY mvnw pom.xml ./

RUN chmod +x mvnw
RUN ./mvnw -B -DskipTests dependency:go-offline

COPY src src

RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:25-jre

WORKDIR /app

RUN groupadd --system app && \
    useradd --system --gid app app

COPY --from=build /app/target/*.jar app.jar

USER app

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java -jar /app/app.jar --server.address=0.0.0.0 --server.port=${PORT:-10000}"]
