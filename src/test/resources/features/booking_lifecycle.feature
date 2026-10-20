@booking
Feature: Booking lifecycle
  Guests' bookings can be created and read by anyone;
  changing or deleting a booking requires an admin token.

  @smoke @severity=critical
  Scenario: Create a booking
    Given a valid booking
    When I create the booking
    Then the response status code should be 200
    And the response should match the "created-booking" schema
    And the booking in the response should match the request

  Scenario Outline: Bookings are accepted for different prices and deposit states
    Given a booking with:
      | totalprice   | depositpaid   |
      | <totalprice> | <depositpaid> |
    When I create the booking
    Then the response status code should be 200
    And the booking in the response should match the request

    Examples:
      | totalprice | depositpaid |
      | 1          | false       |
      | 250        | true        |
      | 99999      | true        |

  @smoke
  Scenario: Retrieve an existing booking
    Given an existing booking
    When I retrieve the booking
    Then the response status code should be 200
    And the response should match the "booking" schema
    And the booking in the response should match the request

  Scenario: Replace a booking with a full update
    Given an existing booking
    And I am authenticated as admin
    When I update the booking with new details
    Then the response status code should be 200
    And the response should match the "booking" schema
    And the booking in the response should match the request

  Scenario: Change selected fields with a partial update
    Given an existing booking
    And I am authenticated as admin
    When I partially update the booking with:
      | firstname       | Budi          |
      | totalprice      | 480           |
      | additionalneeds | Late checkout |
    Then the response status code should be 200
    And the booking should contain:
      | firstname       | Budi          |
      | totalprice      | 480           |
      | additionalneeds | Late checkout |

  Scenario: Delete a booking
    Given an existing booking
    And I am authenticated as admin
    When I delete the booking
    # The API confirms deletion with 201 Created (documented API behavior)
    Then the response status code should be 201
    And the booking should no longer exist

  @negative
  Scenario: Updating a booking without a token is forbidden
    Given an existing booking
    When I update the booking without authentication
    Then the response status code should be 403

  @negative
  Scenario: Requesting a booking that does not exist
    When I retrieve booking with id 999999999
    Then the response status code should be 404