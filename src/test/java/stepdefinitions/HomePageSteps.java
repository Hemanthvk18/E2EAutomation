package stepdefinitions;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import managers.TestContextManager;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import pages.HomePage;
import utilities.FileConstants;
import utilities.SikuliUtility;

import java.io.File;
import java.io.IOException;
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

    @When("user searches for {string} in product filter")
    public void userSearchesForInProductFilter(String productName) {
        Assert.assertTrue((homePage.searchProduct(productName)), "Failed to search for product: " + productName);
    }

    @Then("user should see the search results for {string} in homepage")
    public void userShouldSeeTheSearchResultsForInHomepage(String productName) {
        Assert.assertTrue((homePage.validateSearchedProduct(productName)), "Failed to validate searched product: " + productName);

    }

    @And("user should see the following action buttons for each product as {string}")
    public void userShouldSeeTheFollowingActionButtonsForEachProductAs(String productName, DataTable dataTable) {
        List<String> buttonNames = dataTable.asList(String.class);
        for (String buttonName : buttonNames) {
            Assert.assertTrue((homePage.validateActionButtonsForProduct(productName, buttonName)), "Failed to validate " + buttonName + " button for " + productName + " product");
        }
    }

    @And("user clicks on Cart button in homepage")
    public void userClicksOnCartButtonInHomepage() {
        context.getCustomActions().customClick(homePage.getCartButtonWebElement(), "Cart button in header");
    }

    @Then("verify exact product image present in the homepage using sikuli")
    public void verifyExactProductImagePresentInTheHomepageUsingSikuli(DataTable dataTable) throws IOException {
        List<String> productImageNames = dataTable.asList(String.class);
        Assert.assertTrue(homePage.checkProductImagesVisible(), "Product images are not visible in homepage");

        //To get actual image from the webpage and save in local folder for comparison
//        for (String imageName : productImageNames) {
//            WebElement adidasImage = homePage.getProductImageList().get(2);
//            File source = adidasImage.getScreenshotAs(OutputType.FILE);
//            FileUtils.copyFile(source, new File(FileConstants.IMAGE_DIRECTORY + "/actualAdidas.png"));
//        }


        for (String imageName : productImageNames) {
            String imagePath = FileConstants.IMAGE_DIRECTORY + imageName.trim() + ".png";
            WebElement adidasImage = homePage.getProductImageList().get(0);

            Assert.assertTrue(SikuliUtility.verifyImage(adidasImage, imagePath, imageName));
//            Assert.assertTrue(SikuliUtility.verifyImage(imagePath, imageName), "Failed to verify image: " + imageName);
        }

    }
}
