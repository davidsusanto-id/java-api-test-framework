package com.davidsusanto.api.context;

import com.davidsusanto.api.model.Booking;
import io.restassured.response.Response;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * State shared between the step classes of one scenario.
 * PicoContainer creates a fresh instance per scenario, so parallel scenarios never see each other's data.
 */
public class ScenarioContext {

    private Response response;
    private Booking expectedBooking;
    private Integer bookingId;
    private String token;
    private final Set<Integer> bookingsToCleanUp = new LinkedHashSet<>();

    public Response response() {
        return require(response, "No request has been sent in this scenario yet");
    }

    public void response(Response response) {
        this.response = response;
    }

    public Booking expectedBooking() {
        return require(expectedBooking, "No booking has been prepared in this scenario");
    }

    public void expectedBooking(Booking booking) {
        this.expectedBooking = booking;
    }

    public int bookingId() {
        return require(bookingId, "No booking has been created in this scenario");
    }

    public void bookingCreated(int id) {
        this.bookingId = id;
        bookingsToCleanUp.add(id);
    }

    public void bookingDeleted(int id) {
        bookingsToCleanUp.remove(id);
    }

    public Set<Integer> bookingsToCleanup() {
        return Set.copyOf(bookingsToCleanUp);
    }

    /**
     * May be null: unauthenticated requests are a valid scenario.
     */
    public String token() {
        return token;
    }

    public void token(String token) {
        this.token = token;
    }

    private static <T> T require(T value, String message) {
        if (value == null) {
            throw new IllegalStateException(message + " (check the scenario's Given steps");
        }

        return value;
    }
}
