package com.davidsusanto.api.client;

import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class HealthClient {

    public Response ping() {
        return given().spec(Specs.reported())
                .when().get("/ping");
    }
}
