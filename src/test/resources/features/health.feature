@health
Feature: Service health
  The suite checks service readiness before any scenario
  makes the health check itself visible in the report.

  @smoke @severity=blocker
  Scenario: The API reports that it is up
    When I ping the API
    # Restful-Booker answers /ping with 201 Created rather than 200 (documented API behavior)
    Then the response status code should be 201