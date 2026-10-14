package com.davidsusanto.api.client;

import com.davidsusanto.api.config.TestConfig;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/**
 * Builds request specifications.
 * A new spec is built per request and no static RestAssured state is touched,
 * so parallel scenarios can't interfere with each other.
 */
public final class Specs {

    private Specs() {

    }

    /**
     * Standard spec: every request and response is attached to the Allure report.
     */
    public static RequestSpecification reported() {
        return base()
                .addFilter(new AllureRestAssured()).build();
    }

    /**
     * For framework plumbing (e.g. readiness pooling) that shouldn't clutter the report.
     */
    public static RequestSpecification unreported() {
        return base().build();
    }

    private static RequestSpecBuilder base() {
        TestConfig config = TestConfig.get();

        return new RequestSpecBuilder()
                .setBaseUri(config.baseUrl())
                .setContentType(ContentType.JSON)
                // Restful-Booker matcher the Accept header exactly and answers 418 to anything else,
                // so send the bare value rather than RestAssured's multi-value JSON default.
                .setAccept("application/json")
                .setConfig(RestAssuredConfig.config().httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", config.connectTimeoutMs())
                        .setParam("http.socket.timeout", config.readTimeoutMs())));
    }
}
