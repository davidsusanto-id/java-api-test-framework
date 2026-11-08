# Runs the suite in a reproducible container: same JDK and Gradle everywhere.
#
#   docker build -t booker-api-tests .
#   docker run --rm -v "$PWD/build/allure-results:/app/build/allure-results" booker-api-tests
#   docker run --rm booker-api-tests -Dcucumber.filter.tags="@smoke"

FROM eclipse-temurin:25-jdk-jammy

WORKDIR /app

# Build files first: this layer, with the Gradle distribution and depedency cache,
# is reused until the build definition changes, so editing tests rebuilds in seconds.
COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle ./gradle
RUN chmod +x gradlew \
    && ./gradlew --no-daemon --quiet dependencies --configuration testRuntimeClasspath > /dev/null

COPY src ./src
RUN ./gradlew --no-daemon --quiet compileTestJava

ENV TEST_ENV=public

# Extra arguments to `docker run` are passed to Gradle.
ENTRYPOINT ["./gradlew", "--no-daemon", "test"]