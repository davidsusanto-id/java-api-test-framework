package com.davidsusanto.api.hooks;

import com.davidsusanto.api.client.AuthClient;
import com.davidsusanto.api.client.BookingClient;
import com.davidsusanto.api.context.ScenarioContext;
import com.davidsusanto.api.support.AllureEnvironment;
import com.davidsusanto.api.support.ServiceReadiness;
import io.cucumber.java.After;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import java.lang.System.Logger;

public class Hooks {

    private static final Logger LOG = System.getLogger(Hooks.class.getName());

    private final ScenarioContext context;
    private final AuthClient authClient;
    private final BookingClient bookingClient;

    public Hooks(ScenarioContext context, AuthClient authClient, BookingClient bookingClient) {
        this.context = context;
        this.authClient = authClient;
        this.bookingClient = bookingClient;
    }

    /**
     * Runs once per test run, before the parallel scenarios start.
     */
    @BeforeAll
    public static void beforeAll() {
        AllureEnvironment.write();
        ServiceReadiness.await();
    }

    /**
     * Deletes whatever the scenario created.
     * The API is shared, so leaving data behind is bad manners;
     * a cleanup failure is logged rather than failing a scenario that already passed.
     */
    @After
    public void deleteCreatedBookings(Scenario scenario) {
        if (context.bookingsToCleanup().isEmpty()) {
            return;
        }

        try {
            String token = authClient.adminToken();

            context.bookingsToCleanup().forEach(id -> bookingClient.delete(id, token));
        } catch (RuntimeException e) {
            LOG.log(Logger.Level.WARNING, "Cleanup failed after ''{0}'': {1}", scenario.getName(), e.getMessage());
        }
    }
}
