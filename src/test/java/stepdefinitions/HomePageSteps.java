package stepdefinitions;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import managers.TestContextManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import pages.HomePage;

import java.util.List;

public class HomePageSteps {
    private static final Logger logger = LoggerFactory.getLogger(HomePageSteps.class);

    TestContextManager context;
    HomePage homePage;

    public HomePageSteps(TestContextManager context) {
        this.context = context;
        this.homePage = context.getPageObjectManager().getHomePage();
    }


    @Given("admin user is logged in to the application")
    public void adminUserIsLoggedInToTheApplication() {
        Assert.assertTrue(homePage.checkHomeVisible(), "Failed to land on homepage");
    }

    @When("admin searches for {string} in product filter")
    public void adminSearchesForInProductFilter(String productName) {
        Assert.assertTrue((homePage.searchProduct(productName)), "Failed to search for product: " + productName);
    }

    @Then("admin should see the search results for {string} in homepage")
    public void adminShouldSeeTheSearchResultsForInHomepage(String productName) {
        Assert.assertTrue((homePage.validateSearchedProduct(productName)), "Failed to validate searched product: " + productName);

    }

    @And("admin should see the following action buttons for each product as {string}")
    public void adminShouldSeeTheFollowingActionButtonsForEachProductAs(String productName, DataTable dataTable) {
        List<String> buttonNames = dataTable.asList(String.class);
        for (String buttonName : buttonNames) {
            Assert.assertTrue((homePage.validateActionButtonsForProduct(productName, buttonName)), "Failed to validate " + buttonName + " button for " + productName + " product");
        }
    }
}
