# Usamos una imagen base de OpenJDK para ejecutar aplicaciones Java
FROM openjdk:17-jdk-slim

# Establecemos el directorio de trabajo en el contenedor
WORKDIR /app

# Copiamos el archivo JAR de la API
COPY ./target/Query-Engine-1.0-SNAPSHOT.jar /app/Query-Engine-1.0-SNAPSHOT.jar

COPY ./jsonDatamart /data/datamart

# Copiamos los datos del datalake
COPY ./Datalake /data/datalake

# Copiamos los datos del datamart

# Exponemos el puerto en el que la API va a escuchar las solicitudes
EXPOSE 8080
EXPOSE 4567

CMD ["java","-Xmx1g","-Xmx6g", "-jar", "Query-Engine-1.0-SNAPSHOT.jar"]
