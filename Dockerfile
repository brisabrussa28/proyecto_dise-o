# Etapa de build
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -Dmaven.test.skip=true -Dcheckstyle.skip=true

# Etapa de runtime
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/target/ejercicio-1.0-SNAPSHOT.jar app.jar

ENV PORT=9001
EXPOSE 9001

CMD ["java", "-jar", "app.jar"]