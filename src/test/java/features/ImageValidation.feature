@admin @module:Image @APILogin
Feature: Image Validation using Sikuli

  Background: Admin user is logged in
    Given admin user is logged in to the application

  Rule: Validation of image through sikuli
    @imageValidation
    Scenario:
      Then verify exact product image present in the homepage using sikuli
        | ADIDAS ORIGINAL |
        | ZARA COAT 3     |
        | iphone 13 pro   |
        | Google          |