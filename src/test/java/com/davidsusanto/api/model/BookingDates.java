package com.davidsusanto.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;

/**
 * Dates travel as ISO {@code yyyy-MM-dd} strings,
 * exactly as the API sends and expects them.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BookingDates(String checkin, String checkout) {

    public static BookingDates of(LocalDate checkin, LocalDate checkout) {
        return new BookingDates(checkin.toString(), checkout.toString());
    }
}
