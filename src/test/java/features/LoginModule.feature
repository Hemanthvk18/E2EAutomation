@LoginPage @Regression @admin @module:Login
Feature: Validate Login Functionality

  @ValidLogin @Smoke
  Scenario: User is logged in
    Given admin user is logged in to the application
    Then user navigates to "dash" page