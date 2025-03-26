# ----------------------------------------
# Build stage (usando Maven y JDK 17)
# ----------------------------------------
FROM maven:3.8.6-eclipse-temurin-17 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Cachea las dependencias (se reusa si no cambia el pom.xml)
RUN mvn dependency:go-offline -B

# Empaqueta la aplicación (omitimos tests para el build)
RUN mvn package -DskipTests

# ----------------------------------------
# Runtime stage (usando solo JRE 17 - más ligero)
# ----------------------------------------
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copiamos el JAR desde la etapa de build
COPY --from=build /app/target/*.jar ./app.jar

# Variables importantes para Railway
ENV PORT=8080
EXPOSE $PORT

# Comando de ejecución (usando variable PORT)
ENTRYPOINT ["sh", "-c", "java -jar -Dserver.port=${PORT} app.jar"]