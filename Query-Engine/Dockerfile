# Use the official OpenJDK 17 image as the base image
FROM openjdk:17-jdk-slim

# Establish the working directory in the container
WORKDIR /app

# Copy the JAR file of the API
COPY ./target/Query-Engine-1.0-SNAPSHOT.jar /app/Query-Engine-1.0-SNAPSHOT.jar

COPY ./jsonDatamart /data/datamart

# Copiamos los datos del datalake
COPY ./Datalake /data/datalake

# Copy the data from the datamart

# Expose the port in which the API will listen for requests
EXPOSE 8080
EXPOSE 4567

CMD ["java","-Xmx1g","-Xmx6g", "-jar", "Query-Engine-1.0-SNAPSHOT.jar"]
