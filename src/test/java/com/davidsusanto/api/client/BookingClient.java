package com.davidsusanto.api.client;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

/**
 * One method per endpoint;
 * no assertions here, so steps decide what "correct" means.
 */
public class BookingClient {

    private static final String BOOKING_BY_ID = "/booking/{id}";

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
