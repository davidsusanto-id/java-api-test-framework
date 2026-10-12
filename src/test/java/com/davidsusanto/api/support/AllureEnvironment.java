package com.davidsusanto.api.support;

import com.davidsusanto.api.config.TestConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Fills the report's Environment widget and installs the failure categories.
 */
public final class AllureEnvironment {

    public static void write() {
        Path results = Path.of(System.getProperty("allure.results.directory", "build/allure-results"));
        TestConfig config = TestConfig.get();
        String environment = String.join(System.lineSeparator(),
                "Environment=", config.environment(),
                "Base.URL=" + config.baseUrl(),
                "Java=" + System.getProperty("java.version"),
                "OS=" + System.getProperty("os.name"));

        try {
            Files.createDirectories(results);
            Files.writeString(results.resolve("environment.properties"), environment);

            try (InputStream categories = AllureEnvironment.class.getClassLoader()
                    .getResourceAsStream("allure/categories.json")) {
                        if (categories != null) {
                            Files.copy(categories, results.resolve("categories.json"), StandardCopyOption.REPLACE_EXISTING);
                        }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write Allure environment files to " + results, e);
        }
    }
}
