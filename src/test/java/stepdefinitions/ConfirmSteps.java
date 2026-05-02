package stepdefinitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import managers.TestContextManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import pages.ConfirmPage;
import pages.HomePage;

public class ConfirmSteps {
    private static final Logger logger = LoggerFactory.getLogger(ConfirmSteps.class);

    TestContextManager context;
    ConfirmPage confirmPage;


    public ConfirmSteps(TestContextManager context) {

        this.context = context;
        this.confirmPage=context.getPageObjectManager().getConfirmPage();
    }


    @Then("verify successfully order message {string}")
    public void verifySuccessfullyOrderMessage(String message) {
        Assert.assertEquals(confirmPage.getOrderSuccessMessage(),message);

    }

    @And("save order number for {string}")
    public void saveOrderNumberForFutureReference(String productName) {
        String orderNumber = confirmPage.getOrderNumber();
        context.put(productName + "_orderNumber", orderNumber);
        logger.info("Order number for {}: {}", productName, context.get(productName + "_orderNumber"));
    }
}
