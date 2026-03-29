#Feature: User Login
#
## For admin users
#  @admin @module:HomePage @NA
#  Scenario: Admin user can login
#    When User logs in
#    Then User should see homepage
#
## For normal users
#  @test_user @module:HomePage @NA
#  Scenario: Normal user can login
#    When User logs in
#    Then User should see homepage
#
## For admin-only features
#  @normal_admin_only @module:AdminDashboard @NA
#  Scenario: Only admin can access dashboard
#    When User logs in
#    Then User should see admin dashboard