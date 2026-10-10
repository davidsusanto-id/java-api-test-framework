import org.gradle.kotlin.dsl.version

plugins {
    // Lets Gradle download a matching JDK if the machine doesn't have Java 21.
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "java-api-test-framework"