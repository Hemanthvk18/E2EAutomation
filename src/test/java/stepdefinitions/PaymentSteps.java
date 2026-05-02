package stepdefinitions;

import io.cucumber.java.en.When;
import managers.TestContextManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import pages.PaymentPage;

public class PaymentSteps {
    private static final Logger logger = LoggerFactory.getLogger(PaymentSteps.class);

    TestContextManager context;
    PaymentPage paymentPage;

    public PaymentSteps(TestContextManager context) {
        this.context = context;
        this.paymentPage = context.getPageObjectManager().getPaymentPage();
    }

    @When("user selects {string} in order page")
    public void userSelectsInOrderPage(String countryName) {
        Assert.assertTrue(paymentPage.selectCountry(countryName.trim()),
                "Failed to select country name in cart page : " + countryName);
    }
}
