@auth
Feature: Authentication
  Clients exchange admin credentials for a token a POST /auth
  and send it as a cookie on every write operation.

  @smoke @severity=critical
  Scenario: Admin receives a token for valid credentials
    When I request a token with valid admin credentials
    Then the response status code should be 200
    And the response should match the "auth-token" schema
    And a token should be returned

  @negative
  Scenario Outline: Invalid credentials are rejected (<case>)
    When I request a token with username "<username>" and password "<password>"
    # The API signals rejection in the body and still answers 200, not 401
    Then the response status code should be 200
    And the token request should be rejected with reason "Bad credentials"

    Examples:
      | case           | username | password    |
      | wrong password | admin    | wrong       |
      | unknown user   | nobody   | password123 |
      | empty values   |          |             |