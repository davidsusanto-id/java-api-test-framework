package com.davidsusanto.api.steps;

import com.davidsusanto.api.client.BookingClient;
import com.davidsusanto.api.context.ScenarioContext;
import com.davidsusanto.api.data.BookingBuilder;
import com.davidsusanto.api.model.Booking;
import com.davidsusanto.api.model.CreatedBooking;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;

import java.util.List;
import java.util.Map;

import static com.davidsusanto.api.data.BookingBuilder.aBooking;
import static org.assertj.core.api.Assertions.assertThat;

public class BookingSteps {

    private final ScenarioContext context;
    private final BookingClient bookingClient;

    public BookingSteps(ScenarioContext context, BookingClient bookingClient) {
        this.context = context;
        this.bookingClient = bookingClient;
    }

    // ---------- Given ----------

    @Given("a valid booking")
    public void aValidBooking() {
        context.expectedBooking(aBooking().build());
    }

    /**
     * Header row plus one data row;
     * unspecified fields keep their generated values.
     */
    @Given("a booking with:")
    public void aBookingWith(List<Map<String, String>> rows) {
        context.expectedBooking(aBooking().withOverrides(rows.get(0)).build());
    }

    @Given("an existing booking")
    public void anExistingBooking() {
        aValidBooking();
        iCreateTheBooking();
        context.response().then().statusCode(200);
    }

    // ---------- When ----------

    @When("I create the booking")
    public void iCreateTheBooking() {
        Response response = bookingClient.create(context.expectedBooking());

        context.response(response);
        if (response.statusCode() == 200) {
            context.bookingCreated(response.as(CreatedBooking.class).bookingid());
        }
    }

    @When("I retrieve the booking")
    public void iRetrieveTheBooking() {
        context.response(bookingClient.get(context.bookingId()));
    }

    @When("I retrieve booking with id {int}")
    public void iRetrieveBookingWithId(int id) {
        context.response(bookingClient.get(id));
    }

    @When("I update the booking with new details")
    public void iUpdateTheBookingWithNewDetails() {
        Booking replacement = aBooking().build();

        context.expectedBooking(replacement);
        context.response(bookingClient.update(context.bookingId(), replacement, context.token()));
    }

    @When("I update the booking without authentication")
    public void iUpdateTheBookingWithoutAuthentication() {
        context.response(bookingClient.update(context.bookingId(), aBooking().build(), null));
    }

    @When("I partially update the booking with:")
    public void iPartiallyUpdateTheBookingWith(Map<String, String> fields) {
        context.response(bookingClient.partialUpdate(
                context.bookingId(), BookingBuilder.patchBody(fields), context.token()));
    }

    @When("I delete the booking")
    public void iDeleteTheBooking() {
        int id = context.bookingId();
        Response response = bookingClient.delete(id, context.token());

        context.response(response);
        if (response.statusCode() == 201) {
            context.bookingDeleted(id);
        }
    }

    // ---------- Then ----------

    @Then("the booking in the response should match the request")
    public void theBookingInTheResponseShouldMatchTheRequest() {
        Response response = context.response();
        // POST wraps the booking as {bookingid, booking};
        // GET and PUT return it bare.
        Booking actual = response.jsonPath().get("bookingid") != null
                ? response.as(CreatedBooking.class).booking()
                : response.as(Booking.class);

        assertThat(actual).usingRecursiveComparison().isEqualTo(context.expectedBooking());
    }

    @Then("the booking should contain:")
    public void theBookingShouldContain(Map<String, String> expected) {
        JsonPath json = context.response().jsonPath();
        SoftAssertions.assertSoftly(soft -> expected.forEach((field, value) ->
                soft.assertThat(json.getString(field)).as(field).isEqualTo(value)));
    }

    @Then("the booking should no longer exist")
    public void theBookingShouldNoLongerExist() {
        bookingClient.get(context.bookingId())
                .then().statusCode(404);
    }
}
