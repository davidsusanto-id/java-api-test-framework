package com.davidsusanto.api.support;

import com.davidsusanto.api.client.Specs;
import com.davidsusanto.api.config.TestConfig;

import java.time.Duration;
import java.time.Instant;

import static io.restassured.RestAssured.given;

/**
 * Waits for /ping before any scenario runs.
 * Free hosting tiers sleep when idle and a fresh Docker container needs a few seconds to start;
 * without this the first scenarios fail for reasons that have nothing to do with the API under test.
 */

public final class ServiceReadiness {

    private static final Duration POLL_INTERVAL = Duration.ofSeconds(2);

    private ServiceReadiness() {

    }

    public static void await() {
        TestConfig config = TestConfig.get();
        Instant deadline = Instant.now().plusSeconds(config.readinessTimeoutSeconds());
        String lastResult = "no response";

        while (Instant.now().isBefore(deadline)) {
            try {
                int status = given()
                                .spec(Specs.unreported())
                                .when()
                                    .get("/ping")
                                    .statusCode();

                if (status >= 200 & status < 300) {
                    return;
                }

                lastResult = "HTTP " + status;
            } catch (Exception e) {
                lastResult = e.getClass().getSimpleName() + ": " + e.getMessage();
            }
            sleep();
        }
        throw new IllegalStateException("%s was not ready within %ds (last result: %s)"
                .formatted(config.baseUrl(), config.readinessTimeoutSeconds(), lastResult));
    }

    private static void sleep() {
        try {
            Thread.sleep(POLL_INTERVAL);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for the service", e);
        }
    }
}
