FROM openjdk:24

# Set the working directory
WORKDIR /app

# Copy the packaged jar file into the container
COPY target/exchange-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the app runs on
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]