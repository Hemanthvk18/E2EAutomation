package stepdefinitions;

import api.services.CartService;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import managers.TestContextManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import utilities.AllureUtility;
import utilities.ConfigReader;

import java.util.List;

public class ApiSteps {
    private static final Logger logger = LoggerFactory.getLogger(ApiSteps.class);

    TestContextManager context;
    CartService cartService;

    public ApiSteps(TestContextManager context) {
        this.context = context;
        this.cartService = context.getPageObjectManager().getCartService();
    }


    @Given("user adds product to cart using API")
    public void userAddsProductToCartUsingAPI(DataTable dataTable) throws InterruptedException {

        List<String> productNames = dataTable.asList();
        for(String productName:productNames) {
            Assert.assertEquals(cartService.addProductToCart(context.getToken(), context.getUserId(), productName),
                    "Product Added To Cart");

            logger.info("Product added to cart successfully using API for product : " + productName);
        }

        context.getDriver().navigate().to("https://rahulshettyacademy.com/client/#/dashboard/cart");
        context.getDriver().navigate().refresh(); // To ensure latest cart data is fetched, can be replaced with better wait
        context.getCustomActions().waitForPageLoad();
        Thread.sleep(30000); // Just to ensure products are loaded in UI before screenshot, can be replaced with better wait
        AllureUtility.captureScreenshot(context.getDriver(), "Added Products in Cart");

    }
}
