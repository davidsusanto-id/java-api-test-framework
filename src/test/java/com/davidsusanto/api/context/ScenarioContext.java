package com.davidsusanto.api.context;

import io.restassured.response.Response;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * State shared between the step classes of one scenario.
 * PicoContainer creates a fresh instance per scenario, so parallel scenarios never see each other's data.
 */
public class ScenarioContext {

    private Response response;
    private Integer bookingId;
    private final Set<Integer> bookingsToCleanUp = new LinkedHashSet<>();

    public Response response() {
        return require(response, "No request has been sent in this scenario yet");
    }

    public void response(Response response) {
        this.response = response;
    }

    public void bookingCreated(int id) {
        this.bookingId = id;
        bookingsToCleanUp.add(id);
    }

    public Set<Integer> bookingsToCleanup() {
        return Set.copyOf(bookingsToCleanUp);
    }

    private static <T> T require(T value, String message) {
        if (value == null) {
            throw new IllegalStateException(message + " (check the scenario's Given steps");
        }

        return value;
    }
}
