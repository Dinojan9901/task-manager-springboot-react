# ---- Build stage: full JDK + Maven wrapper ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy only the build files first so the dependency download is cached
# and only re-runs when pom.xml changes, not on every source edit.
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src src
# Tests run in CI; skip them here to keep image builds fast.
RUN ./mvnw -B -q package -DskipTests

# ---- Runtime stage: slim JRE only, no build tools or source code ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Don't run as root inside the container
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
