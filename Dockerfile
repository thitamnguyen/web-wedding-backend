FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre

WORKDIR /app

ENV PORT=7860
ENV JAVA_OPTS=""

COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 7860

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar --server.port=${PORT} --server.address=0.0.0.0"]
