# ----------------------------------------
# Build stage (usando Maven y JDK 17)
# ----------------------------------------
FROM maven:3.8.6-eclipse-temurin-17 AS build

WORKDIR /app
COPY pom.xml .  
COPY src ./src  

# Cachea las dependencias
RUN mvn dependency:go-offline -B

# Empaqueta la aplicación (omitimos tests)
RUN mvn package -DskipTests

# ----------------------------------------
# Runtime stage (usando solo JRE 17 - más ligero)
# ----------------------------------------
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copiamos el JAR desde la etapa de build
COPY --from=build /app/target/*.jar ./app.jar

# Copiar el archivo .env al contenedor final
COPY .env .env

# Variables importantes para Railway
ENV PORT=8080
EXPOSE $PORT

# Cargar variables de .env antes de ejecutar la app
ENTRYPOINT ["sh", "-c", "export $(grep -v '^#' .env | xargs) && java -jar -Dserver.port=${PORT} app.jar"]
