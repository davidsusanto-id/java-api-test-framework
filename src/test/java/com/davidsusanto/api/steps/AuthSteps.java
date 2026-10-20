package com.davidsusanto.api.steps;

import com.davidsusanto.api.client.AuthClient;
import com.davidsusanto.api.config.TestConfig;
import com.davidsusanto.api.context.ScenarioContext;
import com.davidsusanto.api.model.AuthResponse;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthSteps {

    private final ScenarioContext context;
    private final AuthClient authClient;

    public AuthSteps(ScenarioContext context, AuthClient authClient) {
        this.context = context;
        this.authClient = authClient;
    }

    @Given("I am authenticated as admin")
    public void iAmAuthenticatedAsAdmin() {
        context.token(authClient.adminToken());
    }

    @When("I request a token with valid admin credentials")
    public void iRequestATokenWithValidAdminCredentials() {
        TestConfig config = TestConfig.get();

        context.response(authClient.createToken(config.adminUsername(), config.adminPassword()));
    }

    @When("I request a token with username {string} and password {string}")
    public void iRequestATokenWith(String username, String password) {
        context.response(authClient.createToken(username, password));
    }

    @Then("a token should be returned")
    public void aTokenShouldBeReturned() {
        AuthResponse body = context.response().as(AuthResponse.class);

        assertThat(body.token()).as("token").isNotBlank();
        assertThat(body.reason()).as("rejection reason").isNull();
    }

    @Then("the token request should be rejected with reason {string}")
    public void theTokenRequestShouldBeRejected(String reason) {
        AuthResponse body = context.response().as(AuthResponse.class);

        assertThat(body.token()).as("token").isNull();
        assertThat(body.reason()).as("rejection reason").isEqualTo(reason);
    }
}
