# ============================================================================
# Backend Spring Boot — contenedor cloud-agnostic (RNF-39)
# Mismo contenedor desplegable en Railway, Koyeb, Fly.io, Fargate o Cloud Run
# configurado 100% por variables de entorno.
# ============================================================================

# --- Etapa 1: build con Maven + JDK 21 ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cachear dependencias: si el pom no cambia, no se re-descargan
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

COPY src ./src
RUN mvn -q -B package -DskipTests

# --- Etapa 2: runtime liviano solo con JRE 21 ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=build /app/target/*.jar app.jar

# Railway/Koyeb inyectan PORT; application.yml lo lee con ${PORT:8080}
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
