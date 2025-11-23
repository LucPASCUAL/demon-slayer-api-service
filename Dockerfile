FROM maven:3.9.11-amazoncorretto-25 AS build

WORKDIR /app

# First, copy the pom.xml file to take advantage of the Docker cache
COPY pom.xml .

# Download dependencies, -B used for batch mode (no user interaction)
RUN mvn dependency:go-offline -B

# Copy the source code
COPY src ./src

RUN mvn clean package -DskipTests

# Distroless images use a non-root user by default
# They are lightweight container base images containing only the runtime dependencies needed to run an application
# Unlike Ubuntu or Alpine, they do NOT include:
#   - A package manager (apt, yum)
#   - A shell (bash, sh)
#   - Debugging tools (curl, wget, vi)
# It uses a non-root user by default, so there is no need to create a user.
FROM gcr.io/distroless/java25:e59adb610bb41d5f935fe8179a7b9d705df2869b AS runtime

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
