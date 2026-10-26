package com.davidsusanto.api.data;

import com.davidsusanto.api.model.Booking;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.davidsusanto.api.data.BookingBuilder.aBooking;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the test-data layer itself:
 * a builder that produces bad data makes every scenario lie.
 */
class BookingBuilderTest {

    @Test
    void generatesACompleteBookingWithCheckoutAfterCheckin() {
        Booking booking = aBooking().build();

        assertThat(booking).hasNoNullFieldsOrProperties();
        assertThat(LocalDate.parse(booking.bookingdates().checkout()))
                .isAfter(LocalDate.parse(booking.bookingdates().checkin()));
    }

    @Test
    void generatesUniqueLastnamesSoParallelScenariosCannotCollide() {
        Set<String> lastnames = IntStream.range(0, 500)
                .mapToObj(i -> aBooking().build().lastname())
                .collect(Collectors.toSet());

        assertThat(lastnames).hasSize(500);
    }

    @Test
    void overridesOnlyTheFieldsProvided() {
        Booking booking = aBooking()
                .withOverrides(Map.of("totalprice", "42", "depositpaid", "false"))
                .build();

        assertThat(booking.totalprice()).isEqualTo(42);
        assertThat(booking.depositpaid()).isFalse();
        assertThat(booking.firstname()).isNotBlank();
    }

    @Test
    void blankOverrideKeepsTheGeneratedValue() {
        Booking booking = aBooking().withOverrides(Map.of("firstname", " ")).build();

        assertThat(booking.firstname()).isNotBlank();
    }

    @Test
    void rejectsUnknownFields() {
        assertThatThrownBy(() -> aBooking().withOverrides(Map.of("roomnumber", "12")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown booking field 'roomnumber'");
    }

    @Test
    void rejectsAmbiguousBooleans() {
        assertThatThrownBy(() -> aBooking().withOverrides(Map.of("depositpaid", "yes")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("depositpaid must be true or false");
    }

    @Test
    void buildsTypedPatchBodies() {
        assertThat(BookingBuilder.patchBody(Map.of("totalprice", "300")))
                .containsEntry("totalprice", 300);
    }
}
