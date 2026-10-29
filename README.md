# Java API Test Framework

A Java API test automation framework built with REST Assured, Cucumber, JUnit 5, Gradle and Allure, running against the
public [Restful-Booker](https://restful-booker.herokuapp.com/apidoc/index.html) API.

## Quick Start

Requires JDK 21 (Gradle downloads one automatically if it's missing).

```bash
./gradlew test                                  # full suite against the public API
./gradlew test -Dcucumber.filter.tags="@smoke"  # only smoke scenarios
./gradlew test -Dcucumber.filter.tags="@booking and not @negative"
```

View the report locally with the [Allure CLI](https://allurereport.org/docs/install/):

```bash
allure serve build/alllure-results
```

A plain Cucumber HTML report is also written to `build/reports/cucumber/cucumber.html`.

## How it is built

```mermaid
flowchart LR
    F[Feature files] --> S[Step definitions]
    S --> C[ScenarioContext<br/>one per scenario]
    S --> D[Test data builders]
    S --> A[API clients]
    A --> R[Request specs<br/>config, timeouts, Allure filter]
    R --> API[(Restful-Booker)]
    S --> V[Assertions<br/>AssertJ and JSON Schema]
```

```
java-api-test-framework/
├── gradle/
│   ├── wrapper/
│   │   ├── gradle-wrapper.jar                              Gradle wrapper binary
│   │   └── gradle-wrapper.properties                       Pins Gradle 9.3.0
│   └── libs.versions.toml                                  Version catalog: all dependency versions in one place
├── src/
│   └── test/
│       ├── java/
│       │   └── com/
│       │       └── davidsusanto/
│       │           └── api/
│       │               ├── client/
│       │               │   ├── AuthClient.java             POST /auth, admin token helper
│       │               │   ├── BookingClient.java          /booking endpoints (search, get, create, update, patch, delete)
│       │               │   ├── HealthClient.java           GET /ping
│       │               │   └── Specs.java                  Request specs: base URL, headers, timeouts, Allure filter
│       │               ├── config/
│       │               │   ├── TestConfig.java             Environment selection and -D / env-var overrides
│       │               │   └── TestConfigTest.java         JUnit 5 unit test for config key mapping
│       │               ├── context/
│       │               │   └── ScenarioContext.java        Per-scenario state, injected by PicoContainer
│       │               ├── data/
│       │               │   ├── BookingBuilder.java         Test data builder with unique, parallel-safe data
│       │               │   └── BookingBuilderTest.java     JUnit 5 unit tests for the builder
│       │               ├── hooks/
│       │               │   └── Hooks.java                  Run setup (@BeforeAll) and data cleanup (@After)
│       │               ├── model/
│       │               │   ├── AuthRequest.java            Records matching the API's JSON
│       │               │   ├── AuthResponse.java
│       │               │   ├── Booking.java
│       │               │   ├── BookingDates.java
│       │               │   ├── BookingId.java
│       │               │   └── CreateBooking.java 
│       │               ├── steps/
│       │               │   ├── AuthSteps.java              Authentication steps
│       │               │   ├── BookingSteps.java           Booking and search steps
│       │               │   └── CommonSteps.java            Status code, schema validation, ping
│       │               ├── support/
│       │               │   ├── AllureEnvironment.java      Writes report environment info and failure categories
│       │               │   └── ServiceReadiness.java       Waits for /ping before the run starts
│       │               └── RunCucumberTest.java            JUnit Platform suite that Gradle discovers
│       └── resources/
│           ├── allure/ 
│           │   └── categories.json                         Failure categories shown in the Allure report
│           ├── config/
│           │   ├── local.properties                        Restful-Booker in Docker (localhost:3001)
│           │   └── public.properties                       restful-booker.herokuapp.com (default)
│           ├── features/
│           │   ├── auth.feature
│           │   ├── booking_lifecycle.feature
│           │   ├── booking_search.feature
│           │   └── health.feature
│           ├── schemas/
│           │   ├── auth-token.json                         JSON schemas for each response type
│           │   ├── booking-ids.json
│           │   ├── booking.json
│           │   └── created-booking.json
│           ├── allure.properties                           Allure results directory
│           └── junit-platform.properties                   Cucumber plugins, default tags, parallelism
├── .gitattributes                                          Line endings for gradlew / .bat, jar as binary
├── .gitignore
├── build.gradle.kts                                        Dependencies and test task configuration
├── gradle.properties                                       Gradle JVM and build cache settings
├── gradlew                                                 Gradle wrapper script (macOS / Linux)
├── gradlew.bat                                             Gradle wrapper script (Windows)
├── README.md
└── settings.gradle.kts                                     Project name and JDK auto-download plugin            
```

## Design decisions

**Clients don't assert.** Each client method sends one request and returns the response. Steps decide what correct
means, so the same client serves both positive and negative scenarios (for example `update(id, booking, null)` for the
unauthenticated case).

**State lives in a PicoContainer-scoped `ScenarioContext`.** Cucumber creates a fresh context for every scenario and
injects it into the step classes that need it. There are no static fields holding scenario data, which is what makes
scenario-level parallelism safe. Request specs are also built per request rather than through RestAssured's static
configuration.

**Builders generate valid data; scenarios state only what matters.** `aBooking().build()` returns a complete, realistic
booking. A scenario that cares about price and deposit sets only those two fields in its data table, which keeps the
Gherkin focused on behavior. Every generated last name carries a unique suffix, so a search on the shared public API can
never match another scenario's or another person's booking.

**Tests clean up after themselves.** Bookings created during a scenario are registered in the context and deleted in an
`@After` hook. A cleanup failure is logged rather than failing a scenario that already passed.

**Contracts are checked separately from values.** Schemas use `additionalProperties: false`, so an unexpected new field
fails the contract check even when every asserted value is correct. Value checks use AssertJ's recursive comparison
against the object that was sent.

**The run waits for the service before it starts.** Free hosting sleeps when idle and a new container takes a few
seconds to come up. A readiness check pools `/ping` once before any scenario runs, so cold starts don't show up as test
failures.

**Failures are categorized.** The Allure report groups failures into service unavailable, contract violations,
unexpected status codes, data mismatches and framework detects, so a red run can be triaged at a glance.

## API behavior worth knowing

- `GET /ping` and `DELETE /booking/{id}` answer `201 Created`.
- Invalid credentials at `POST /auth` return `200` with `{"reason": "Bad credentials"}` rather than `401`.
- Missing or invalid tokens on write operations return `403` rather than `401`.
- The `Accept` header must be exactly `application/json`; anything else return `418`.

## Adding a test

1. Write the scenario in `.feature` file, reusing existing steps where possible.
2. If it needs a new endpoint, add a method to the relevant client.
3. Add step definitions to the step class for that domain, keeping state in `ScenarioContext`.
4. Add a schema under `resources/schemas/` for any new response shape.
5. Tag it (`@somke`, `@negative`, a domain tag) so it can be run selectively.