package com.davidsusanto.api.context;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * State shared between the step classes of one scenario.
 * PicoContainer creates a fresh instance per scenario, so parallel scenarios never see each other's data.
 */
public class ScenarioContext {

    private Integer bookingId;
    private final Set<Integer> bookingsToCleanUp = new LinkedHashSet<>();

    public void bookingCreated(int id) {
        this.bookingId = id;
        bookingsToCleanUp.add(id);
    }

    public Set<Integer> bookingsToCleanup() {
        return Set.copyOf(bookingsToCleanUp);
    }
}
