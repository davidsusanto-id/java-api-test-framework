package com.davidsusanto.api.data;

import com.davidsusanto.api.model.Booking;
import com.davidsusanto.api.model.BookingDates;
import net.datafaker.Faker;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Produces valid, unique bookings by default;
 * scenarios override only the fields they care about.
 *
 * <p>Every generated last name carries a short unique suffix.
 * The public API is shared with everyone on the internet
 * and these scenarios run in parallel,
 * so searches by name must never match someone else's data.</p>
 */
public class BookingBuilder {

    private static final ThreadLocal<Faker> FAKER = ThreadLocal.withInitial(Faker::new);
    private static final List<String> ADDITIONAL_NEEDS = List.of(
            "Breakfast",
            "Late checkout",
            "Airport transfer",
            "Extra pillow",
            "Parking"
    );

    private String firstname;
    private String lastname;
    private int totalprice;
    private boolean depositpaid;
    private LocalDate checkin;
    private LocalDate checkout;
    private String additionalneeds;

    private BookingBuilder() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Faker faker = FAKER.get();

        firstname = faker.name().firstName();
        lastname = faker.name().lastName() + "-" + uniqueSuffix();
        totalprice = random.nextInt(50, 2_001);
        depositpaid = random.nextBoolean();
        checkin = LocalDate.now().plusDays(random.nextInt(1, 91));
        checkout = checkin.plusDays(random.nextInt(1, 15));
        additionalneeds = ADDITIONAL_NEEDS.get(random.nextInt(ADDITIONAL_NEEDS.size()));
    }

    public static BookingBuilder aBooking() {
        return new BookingBuilder();
    }

    public BookingBuilder withFirstname(String firstname) {
        this.firstname = firstname;
        return this;
    }

    public BookingBuilder withLastname(String lastname) {
        this.lastname = lastname;
        return this;
    }

    public BookingBuilder withTotalprice(int totalprice) {
        this.totalprice = totalprice;
        return this;
    }

    public BookingBuilder withDepositpaid(boolean depositpaid) {
        this.depositpaid = depositpaid;
        return this;
    }

    /**
     * No ordering check on purpose: negative tests may need checkout before checkin.
     */
    public BookingBuilder withDates(LocalDate checkin, LocalDate checkout) {
        this.checkin = checkin;
        this.checkout = checkout;
        return this;
    }

    public BookingBuilder withAdditionalneeds(String additionalneeds) {
        this.additionalneeds = additionalneeds;
        return this;
    }

    /**
     * Applies overrides from a Gherkin data table, e.g. {@code | totalprice | depositpaid |}.
     * Blank cells keep the generated value.
     */
    public BookingBuilder withOverrides(Map<String, String> overrides) {
        overrides.forEach((field, raw) -> {
            if (raw == null || raw.isBlank()) {
                return;
            }

            switch (field) {
                case "firstname" -> withFirstname(raw);
                case "lastname" -> withLastname(raw);
                case "totalprice" -> withTotalprice((Integer) parse(field, raw));
                case "depositpaid" -> withDepositpaid((Boolean) parse(field, raw));
                case "checkin" -> withDates(LocalDate.parse(raw), checkout);
                case "checkout" -> withDates(checkin, LocalDate.parse(raw));
                case "additionalneeds" -> withAdditionalneeds(raw);
                default -> throw unknownField(field);
            }
        });

        return this;
    }

    public Booking build() {
        return new Booking(
                firstname,
                lastname,
                totalprice,
                depositpaid,
                BookingDates.of(checkin, checkout),
                additionalneeds
        );
    }

    /**
     * Turns a Gherkin key/value table into a typed PATCH body (top-level fields only).
     */
    public static Map<String, Object> patchBody(Map<String, String> fields) {
        Map<String, Object> body = new LinkedHashMap<>();

        fields.forEach((field, raw) -> {
            if (field.equals("checkin") || field.equals("checkout")) {
                throw new IllegalArgumentException("Change dates with a full update, not a partial one");
            }

            body.put(field, parse(field, raw));
        });

        return body;
    }

    private static Object parse(String field, String raw) {
        return switch (field) {
            case "firstname", "lastname", "additionalneeds" -> raw;
            case "totalprice" -> Integer.parseInt(raw);
            case "depositpaid" -> {
                if (!raw.equalsIgnoreCase("true") && !raw.equalsIgnoreCase("false")) {
                    throw new IllegalArgumentException("depositpaid must be true or false, got '" + raw + "'");
                }

                yield Boolean.parseBoolean(raw);
            }
            default -> throw unknownField(field);
        };
    }

    private static IllegalArgumentException unknownField(String field) {
        return new IllegalArgumentException("Unknown booking field '" + field + "'");
    }

    private static String uniqueSuffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
