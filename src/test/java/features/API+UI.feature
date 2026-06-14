@admin @module:API_UI @APILogin
Feature: API Automation using RestAssured
  # Login
# @APILogin --> if we mention this tag, Login will appends through API and we will get the token, which we can use in other API calls

  @VerifyCartInUIAfterAPIAddToCart
  Scenario: Validate Add To Cart and validate the same product in UI
    Given user adds product to cart using API and validate the same product in UI
      | ADIDAS ORIGINAL |
      | ZARA COAT 3     |
      | iphone 13 pro   |

  @VerifyOrderInUIAfterAPIPlaceOrder
  Scenario: Validate Place Order and validate the same order in UI
    Given user adds product to cart using API and validate the same product in UI
      | ADIDAS ORIGINAL |
      | ZARA COAT 3     |
      | iphone 13 pro   |
    Then user places order using API and validate the same order in UI