package com.davidsusanto.api.config;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class TestConfigTest {

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "baseUrl, BASE_URL",
            "admin.username, ADMIN_USERNAME",
            "admin.password, ADMIN_PASSWORD",
            "http.connectTimeoutMs, HTTP_CONNECT_TIMEOUT_MS",
            "readiness.timeoutSeconds, READINESS_TIMEOUT_SECONDS"
    })
    void mapsConfigKeysToEnvironmentVariableName(String key, String envVar) {
        assertThat(TestConfig.toEnvVar(key)).isEqualTo(envVar);
    }
}
