package stepdefinitions;

import api.payloads.response.CreateOrderResponse;
import api.services.CartService;
import api.services.OrderService;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import managers.TestContextManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import pages.CartPage;
import pages.ConfirmPage;
import utilities.AllureUtility;
import utilities.ConfigReader;
import utilities.CustomStringUtils;

import java.util.List;

public class ApiSteps {
    private static final Logger logger = LoggerFactory.getLogger(ApiSteps.class);

    TestContextManager context;
    CartService cartService;
    CartPage cartPage;
    OrderService orderService;
    ConfirmPage confirmPage;

    public ApiSteps(TestContextManager context) {
        this.context = context;
        this.cartService = context.getPageObjectManager().getCartService();
        this.cartPage = context.getPageObjectManager().getCartPage();
        this.orderService=context.getPageObjectManager().getOrderService();
        this.confirmPage=context.getPageObjectManager().getConfirmPage();
    }


    @Given("user adds product to cart using API")
    public void userAddsProductToCartUsingAPI(DataTable dataTable) throws InterruptedException {
        List<String> productNames = dataTable.asList(String.class);
        context.put("addedProducts", productNames);

        // Add products to cart using API and validate response
        for(String productName:productNames) {
            context.put("",productName.trim());
            Assert.assertEquals(cartService.addProductToCart(context.getToken(), context.getUserId(), productName),
                    "Product Added To Cart");

            logger.info("Product added to cart successfully using API for product : {}", productName);
        }

    }

    @Then("user places order using API")
    public void userPlacesOrderUsingAPI() {
        //added products in cart
        List<String> addedProducts = CustomStringUtils.convertToStringList(context.get("addedProducts"));

        //Order products using API and validate response
        CreateOrderResponse response = orderService.createOrder(context.getToken(), addedProducts);
        Assert.assertEquals(
                response.getMessage(),
                "Order Placed Successfully");

    }

    @Given("user adds product to cart using API and validate the same product in UI")
    public void userAddsProductToCartUsingAPIAndValidateTheSameProductInUI(DataTable dataTable) throws InterruptedException {
        List<String> productNames = dataTable.asList(String.class);
        context.put("addedProducts", productNames);

        // Add products to cart using API and validate response
        for(String productName:productNames) {
            context.put("",productName.trim());
            Assert.assertEquals(cartService.addProductToCart(context.getToken(), context.getUserId(), productName),
                    "Product Added To Cart");

            logger.info("Product added to cart successfully using API for product : {}", productName);
        }
        // Navigate to cart page
        context.getDriver().navigate().to("https://rahulshettyacademy.com/client/#/dashboard/cart");
        context.getDriver().navigate().refresh(); // To ensure latest cart data is fetched, can be replaced with better wait
        context.getCustomActions().waitForPageLoad();
        // Verify products are present in cart page
        for (String product : productNames) {
            Assert.assertTrue(cartPage.verifyProductsPresence(product.trim()), "Product not present in cart page, Product Name : " + product);
        }
        AllureUtility.captureScreenshot(context.getDriver(), "Added Products in Cart");
    }

    @Then("user places order using API and validate the same order in UI")
    public void userPlacesOrderUsingAPIAndValidateTheSameOrderInUI() {

        //added products in cart
        List<String> addedProducts = CustomStringUtils.convertToStringList(context.get("addedProducts"));

        //Order products using API and validate response
        CreateOrderResponse response = orderService.createOrder(context.getToken(), addedProducts);
        Assert.assertEquals(
                response.getMessage(),
                "Order Placed Successfully");

        // Navigate to thank page
        context.getDriver().navigate().to("https://rahulshettyacademy.com/client/#/dashboard/thanks");
        context.getDriver().navigate().refresh(); // To ensure latest cart data is fetched, can be replaced with better wait
        context.getCustomActions().waitForPageLoad();

        //Verify order confirmation message in UI
        Assert.assertEquals(confirmPage.getOrderSuccessMessage(),"THANKYOU FOR THE ORDER.");
        logger.info("Order placed successfully using API for product : {}", addedProducts);

        //Save order number for future reference and log to allure
//        String orderNumber = confirmPage.getOrderNumber();
//        context.put( "OrderNumber", orderNumber);
//        AllureUtility.addSubStepForData("Order Number", addedProducts + "_orderNumber", orderNumber);

        AllureUtility.captureScreenshot(context.getDriver(), "Added Products in Cart");

    }
}
