package com.davidsusanto.api.client;

import com.davidsusanto.api.config.TestConfig;
import com.davidsusanto.api.model.AuthRequest;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class AuthClient {

    public Response createToken(String username, String password {
        return given().spec(Specs.reported())
                .body(new AuthRequest(username, password))
                .when().post("/auth");
    }

    /**
     * Logs in with the configured admin account and fails fast if no token comes back.
     */
    public String adminToken() {
        TestConfig config = TestConfig.get();
        Response response = createToken(config.adminUsername(), config.adminPassword());
        String token = response.then().statusCode(200).extract().path("token");

        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Admin login returned no token: " + response.asString());
        }

        return token;
    }
}
