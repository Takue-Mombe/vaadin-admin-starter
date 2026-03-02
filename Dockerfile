FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml ./
COPY src ./src

RUN mvn -Pproduction -DskipTests clean package \
    && cp target/adminpro-*.jar /app/app.jar

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/app.jar /app/app.jar

ENV JAVA_OPTS=""
EXPOSE 8080

CMD ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod} -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
