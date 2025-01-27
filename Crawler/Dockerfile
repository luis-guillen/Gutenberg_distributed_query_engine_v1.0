# Use the official Eclipse Temurin JDK 17 image as the base image
FROM eclipse-temurin:17-jdk-jammy
# Establish the working directory in the container
WORKDIR /app
# Copy the JAR file of the Crawler
COPY target/Crawler-1.0-SNAPSHOT.jar /app/Crawler-1.0-SNAPSHOT.jar
# Define the command to run the application
ENTRYPOINT ["java", "-jar", "Crawler-1.0-SNAPSHOT.jar"]