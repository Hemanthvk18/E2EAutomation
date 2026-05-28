@LoginPage @admin @module:Login @Smoke
Feature: Validate Login Functionality

  @ValidLogin
  Scenario: User is logged in
    Given admin user is logged in to the application
    Then user navigates to "dash" page