package stepdefinitions;

import io.cucumber.java.en.Then;
import managers.TestContextManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.PaymentPage;

public class CommonSteps {
    private static final Logger logger = LoggerFactory.getLogger(CommonSteps.class);

    TestContextManager context;

    public CommonSteps(TestContextManager context) {
        this.context = context;
    }


    @Then("user clicks {string} {string} in {string}")
    public void userClicksIn(String elementText, String tagName, String pageName) {
        context.getCustomActions().clickElementWithText(elementText, tagName);
    }
}
