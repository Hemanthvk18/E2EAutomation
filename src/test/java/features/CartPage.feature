@CartPage @admin @module:CART @APILogin
Feature: Product Search After Admin Login

  Background: Admin user is logged in
    Given admin user is logged in to the application

  Rule: Verify cart page product details
    @VerifyCartPageDetails  @1406
    Scenario Outline: user can see correct product details for "<Product Name>" in cart page after adding product to cart from homepage
      When user adds "<Product Name>" to cart from homepage
      And user clicks on Cart button in homepage
      And user navigates to "cart" page
      Then user should see the correct "<Product Details>" for "<Product Name>" in cart page from "Price Data" sheet
      Examples:
        | Product Name    | Product Details     |
        | ADIDAS ORIGINAL | Price, Availability |
        | ZARA COAT 3     | Price, Availability |
        | iphone 13 pro   | Price, Availability |