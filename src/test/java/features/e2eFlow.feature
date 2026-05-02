@CompletePage @Regression @admin @module:CompeteFlow
Feature: Product Search After Admin Login

  Background: Admin user is logged in
    Given admin user is logged in to the application

  Rule:End to End process of ordering a product from homepage to confirm page
    @orderProduct @Smoke @Regression
    Scenario Outline: Verify the user can order the product "<Product Name>" successfully from homepage to confirm page
      When user adds "<Product Name>" to cart from homepage
      And user navigates to "cart" page
      Then user should see the correct "<Product Name>" in cart page
      And user proceeds to checkout from cart page
      And user navigates to "order" page
      When user selects "<Country Name>" in order page
      Then user clicks "Place Order" "a" in "Order page"
      And user navigates to "thanks" page
      Then verify successfully order message "Thankyou for the order."
      And save order number for "<Product Name>"

      Examples:
        | Product Name                              | Country Name                   |
        | ADIDAS ORIGINAL                           | India                          |
        | ZARA COAT 3                               | British Indian Ocean Territory |
        | iphone 13 pro                             | South Africa                   |
        | ADIDAS ORIGINAL,ZARA COAT 3,iphone 13 pro | Australia                      |