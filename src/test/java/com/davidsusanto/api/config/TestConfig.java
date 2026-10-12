package com.davidsusanto.api.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Locale;
import java.util.Properties;

/**
 * Configuration for the selected environment.
 *
 * <p>The environment is chosen with {@code -Denv=<name>} or the {@code TEST_ENV} variable
 * and map to {@code src/test/resources/config/<name>.properties}.
 * Any single key can be overridden, highest precedence first, by a JVM system property ({@code -DbaseUrl=...})
 * or an environment variable ({@code BASE_URL=...}).
 * That is how CI and Docker inject values, including secrets, without committing them.</p>
 */
public final class TestConfig {

    private static final String DEFAULT_ENV = "public";
    private static final TestConfig INSTANCE = load();

    private final String environment;
    private final Properties properties;

    private TestConfig(String environment, Properties properties) {
        this.environment = environment;
        this.properties = properties;
    }

    public static TestConfig get() {
        return INSTANCE;
    }

    public String environment() {
        return environment;
    }

    public String baseUrl() {
        return required("baseUrl");
    }

    private String required(String key) {
        String value = firstNonBlank(
                System.getProperty(key),
                System.getenv(toEnvVar(key)),
                properties.getProperty(key)
        );
        if (value == null) {
            throw new IllegalStateException(
                    "Missing configuration '%s' for environment '%s'. Set it in config/%s.properties, with -D%s or with %s."
                            .formatted(key, environment, environment, key, toEnvVar(key))
            );
        }
        return value.trim();
    }

    /**
     * {@code baseUrl -> BASE_URL}, {@code admin.username -> ADMIN_USERNAME}
     */
    static String toEnvVar(String key) {
        return key.replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .replace('.', '_')
                .toUpperCase(Locale.ROOT);
    }

    private static TestConfig load() {
        String env = firstNonBlank(System.getProperty("env"), System.getenv("TEST_ENV"), DEFAULT_ENV);
        String resource = "config/" + env + ".properties";
        Properties properties = new Properties();

        try (InputStream in = TestConfig.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Unknown environment '" + env + "': " + resource + " is not on the classpath");
            }
            properties.load(in);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + resource, e);
        }
        return new TestConfig(env, properties);
    }

    private static String firstNonBlank(String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate;
            }
        }
        return null;
    }
}
