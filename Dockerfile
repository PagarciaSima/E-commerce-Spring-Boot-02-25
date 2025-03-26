# Usa una imagen oficial de Java como base
FROM eclipse-temurin:17-jdk AS build

# Establece el directorio de trabajo
WORKDIR /app

# Copia los archivos del proyecto al contenedor
COPY . .

# Da permisos al wrapper de Maven
RUN chmod +x mvnw

# Construye la aplicación sin ejecutar tests
RUN ./mvnw clean package -DskipTests

# Segunda etapa: Imagen más ligera para correr la app
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copia el archivo JAR generado desde la fase de construcción
COPY --from=build /app/target/*.jar app.jar

# Expone el puerto en el que corre la app
EXPOSE 8081

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "app.jar"]
