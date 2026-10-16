package com.davidsusanto.api.steps;

import com.davidsusanto.api.client.HealthClient;
import com.davidsusanto.api.context.ScenarioContext;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * Steps that work on whatever the last response was, regardless of endpoint.
 */
public class CommonSteps {

    private final ScenarioContext context;
    private final HealthClient healthClient;

    public CommonSteps(ScenarioContext context, HealthClient healthClient) {
        this.context = context;
        this.healthClient = healthClient;
    }

    @When("I ping the API")
    public void pingTheApi() {
        context.response(healthClient.ping());
    }

    @Then("the response status code should be {int}")
    public void theResponseStatusCodeShouldBe(int expected) {
        context.response().then().statusCode(expected);
    }
}
