@search
Feature: Booking search
  GET /booking returns the ids of bookings matching optional filters.

  @smoke
  Scenario: Find a booking by the guest's name
    Given an existing booking
    When I search for bookings by the booking's guest name
    Then the response status code should be 200
    And the response should match the "booking-ids" schema
    And the search results should contain the booking

  @negative
  Scenario: A search with no matches returns an empty list
    When I search for a guest that does not exist
    Then the response status code should be 200
    And the search results should be empty