#string literals E2E flow
# Feature: Product Search After Admin Login
# This feature tests that admin users can successfully login and search for products
# Tags: @admin triggers admin user login in Hook, @module:HomePage loads HomePage test data

@LoginPage @Regression @admin @module:HomePage
Feature: Product Search After Admin Login

  Background: Admin user is logged in
    # Hook.beforeScenario() automatically logs in admin user with EMAIL and PASSWORD env vars
    # User is navigated to homepage after successful login
    Given admin user is logged in to the application


    # Prerequisites: Admin is already logged in (via Hook @Before)
  Rule: This scenario tests product search functionality
    @SearchProduct @Smoke
    Scenario Outline: Admin can search for products after login
      When admin searches for "<Product Name>" in product filter
      Then admin should see the search results for "<Product Name>" in homepage
      And admin should see the following action buttons for each product as "<Product Name>"
        | View        |
        | Add To Cart |

      Examples:
        | Product Name    |
        | ADIDAS ORIGINAL |
        | ZARA COAT 3     |
        | iphone 13 pro   |
