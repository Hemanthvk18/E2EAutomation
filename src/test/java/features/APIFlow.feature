@admin @module:API @APILogin @Smoke
Feature: API Automation using RestAssured
  # Login
# @APILogin --> if we mention this tag, Login will appends through API and we will get the token, which we can use in other API calls

  @APIAddToCart
  Scenario: Validate Add To Cart
    Given user adds product to cart using API
      | ADIDAS ORIGINAL |
      | ZARA COAT 3     |
      | iphone 13 pro   |

  @APIPlaceOrder
  Scenario: Validate Place Order
    Then user adds product to cart using API
      | ADIDAS ORIGINAL |
      | ZARA COAT 3     |
      | iphone 13 pro   |
    Then user places order using API