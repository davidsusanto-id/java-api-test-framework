plugins {
    java
}

group = "com.davidsusanto"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {

}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()

    // Forward selected -D flags from the Gradle command line into the test JVM,
    // e.g.
    //    ./gradlew test -Denv=local -Dcucumber.filter.tags="@smoke"
    listOf("env", "baseUrl").forEach { key ->
        providers.systemProperty(key).orNull?.let { systemProperty(key, it) }
    }
    System.getProperties().stringPropertyNames()
        .filter { it.startsWith("cucumber.") }
        .forEach { systemProperty(it, System.getProperty(it)) }

    systemProperty("allure.results.directory", layout.buildDirectory.dir("allure-results").get().asFile.absolutePath)

    // Scenarios run in parallel inside a single JVM (see junit-platform.properties,
    // so one fork is intentional: it keeps one Allure results stream and one readiness check.
    maxParallelForks = 1

    // These tests exercise a live service, so a passing run must never be treated as up to date.
    outputs.upToDateWhen { false }

    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}