package stepdefinitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import managers.TestContextManager;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import pages.CartPage;
import pages.HomePage;
import utilities.AllureUtility;
import utilities.ConfigReader;

import java.util.*;

public class CartSteps extends BaseSteps {
    private static final Logger logger = LoggerFactory.getLogger(CartSteps.class);
    HomePage homePage;
    CartPage cartPage;

    public CartSteps(TestContextManager context) {
        super(context);
        this.homePage = context.getPageObjectManager().getHomePage();
        this.cartPage = context.getPageObjectManager().getCartPage();
    }


    @When("user adds {string} to cart from homepage")
    public void userAddsToCartFromHomepage(String productName) {
        // click on cart button for the product, validate product added to cart alert, wait for it to disappear
        List<String> ProductNames = Arrays.stream(productName.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        for (String product : ProductNames) {
            context.getCustomActions().customClick(homePage.addToCartButtonForProduct(product), "Add to Cart button for product: " + product);
            Assert.assertTrue(homePage.checkProductAddedAlertVisible(), "Failed to validate Product Added To Cart alert visibility after adding product: " + product);
        }
        homePage.waitForProductAddedAlertToDisappear();
    }

    @And("user navigates to {string} page")
    public void userNavigatesToCartPage(String pageName) {
        //Click on cart button
        context.getCustomActions().customClick(homePage.getCartButtonWebElement(), "Cart button in header");
        //verify url
        String expectedUrl = ConfigReader.getConfigReader().getUrl() + "/#/dashboard/" + pageName.toLowerCase();
        context.getWebdriverWait().until(ExpectedConditions.urlContains(expectedUrl));
        String actualUrl = context.getCustomActions().getUrl();
        Assert.assertTrue(actualUrl.contains(expectedUrl), "Failed to navigate to " + pageName + " page");
    }

    @Then("user should see the correct {string} for {string} in cart page from {string} sheet")
    public void userShouldSeeTheCorrectForInCartPage(String productDetails, String productName, String sheetName) {
        var first = first(sheetName);
        List<String> values = Arrays.stream(productDetails.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();

        //log to allure
        Map<String, String> sub = new LinkedHashMap<>();
        for (String value : values) {
            sub.put(value, Optional.ofNullable(first.get(value)).orElse(""));
        }
        AllureUtility.addSubStepForData("Price and Stock Status", sub, values);

        for (String value : values) {
            String expectedValue = Optional.ofNullable(first.get(value)).orElse("");
            System.out.println("------ in excel-------: "+expectedValue);
            if (value.equalsIgnoreCase("Availability")) {
                String actualStockStatus = cartPage.getProductStockStatus(productName);
                System.out.println("------ in UI (actualStockStatus)-------: "+actualStockStatus);
                Assert.assertEquals(actualStockStatus, expectedValue, "Incorrect " + value + " for product: " + productName + " in cart page");
                logger.info("Validated stock status for product: {} in cart page, Expected: {}, Actual: {}", productName, expectedValue, actualStockStatus);
            } else if (value.equalsIgnoreCase("Price")) {
                String actualValue = cartPage.getProductPrice(productName);
                System.out.println("------ in UI (actualValue)-------: "+actualValue);
                Assert.assertEquals(actualValue, expectedValue, "Incorrect " + value + " for product: " + productName + " in cart page");
                logger.info("Validated price for product: {} in cart page, Expected: {}, Actual: {}", productName, expectedValue, actualValue);
            }

        }
    }

    @Then("user should see the correct {string} in cart page")
    public void userShouldSeeTheCorrectInCartPage(String productName) {
        List<String> ProductNames = Arrays.stream(productName.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        for (String product : ProductNames) {
            Assert.assertTrue(cartPage.verifyProductsPresence(product), "Product not present in cart page, Product Name : " + product);
        }
    }

    @And("user proceeds to checkout from cart page")
    public void userProceedsToCheckoutFromCartPage() {
        Assert.assertTrue(cartPage.proceedWithCheckout());
    }


}
