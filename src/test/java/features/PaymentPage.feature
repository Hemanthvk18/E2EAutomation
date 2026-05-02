@PaymentPage @Regression @admin @module:PaymentPage
Feature: Product Search After Admin Login

  Background: Admin user is logged in
    Given admin user is logged in to the application

  Rule: Verify
     @Smoke
    Scenario Outline: user

      Examples:
        | Product Name    | Product Details |
        | ADIDAS ORIGINAL | Price, Quantity |
        | ZARA COAT 3     | Price, Quantity |
        | iphone 13 pro   | Price, Quantity |