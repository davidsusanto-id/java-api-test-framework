package com.davidsusanto.api.client;

import com.davidsusanto.api.model.Booking;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * One method per endpoint;
 * no assertions here, so steps decide what "correct" means.
 */
public class BookingClient {

    private static final String BOOKING = "/booking";
    private static final String BOOKING_BY_ID = "/booking/{id}";

    public Response search(Map<String, ?> filters) {
        return given().spec(Specs.reported()).queryParams(filters)
                .when().get(BOOKING);
    }

    public Response get(int id) {
        return given().spec(Specs.reported()).pathParam("id", id)
                .when().get(BOOKING_BY_ID);
    }

    public Response create(Booking booking) {
        return given().spec(Specs.reported()).body(booking)
                .when().post(BOOKING);

    }

    public Response update(int id, Booking booking, String token) {
        return withToken(token).pathParam("id", id).body(booking)
                .when().put(BOOKING_BY_ID);
    }

    public Response partialUpdate(int id, Map<String, Object> fields, String token) {
        return withToken(token).pathParam("id", id).body(fields)
                .when().patch(BOOKING_BY_ID);
    }

    public Response delete(int id, String token) {
        return withToken(token).pathParam("id", id)
                .when().delete(BOOKING_BY_ID);
    }

    /**
     * A null token sends the request unauthenticated, which negative scenario rely on.
     */
    private RequestSpecification withToken(String token) {
        RequestSpecification spec = given().spec(Specs.reported());

        return token == null ? spec : spec.cookie("token", token);
    }
}
