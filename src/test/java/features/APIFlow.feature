@admin @module:API @APILogin
Feature: API Automation using RestAssured

  @may24
  Scenario: Validate Add To Cart
    Given user adds product to cart using API
      | ADIDAS ORIGINAL |
      | ZARA COAT 3     |
      | IPHONE 13 PRO   |