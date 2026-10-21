# ---------- Etapa 1: compilación ----------
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Primero solo el wrapper y el pom: así Docker cachea las dependencias
# y no las vuelve a descargar si solo cambia el código fuente
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw package -DskipTests -B

# ---------- Etapa 2: ejecución ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app

COPY --from=build /app/target/*.jar app.jar
RUN mkdir -p /app/uploads && chown -R app:app /app

USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]