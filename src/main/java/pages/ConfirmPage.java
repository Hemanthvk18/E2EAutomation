package pages;

import managers.TestContextManager;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfirmPage {
    private static final Logger logger = LoggerFactory.getLogger(ConfirmPage.class);
    TestContextManager context;

    public ConfirmPage(TestContextManager context) {
        this.context = context;
        PageFactory.initElements(context.getDriver(), this);
    }

    @FindBy(xpath = "//h1")
    WebElement orderSuccessMessage;

    @FindBy(xpath = "(//td//label)[2]")
    WebElement orderNumberText;

    public String getOrderSuccessMessage() {
        WebElement locator = context.getCustomWait().waitForElementToBeVisible(orderSuccessMessage, "Success Message");
        return context.getCustomActions().getText(locator);
    }

    public String getOrderNumber() {
        WebElement locator = context.getCustomWait().waitForElementToBeVisible(orderNumberText, "Success Message");
        String input =context.getCustomActions().getText(locator);
        return input.trim().split("\\|")[1].trim();
    }

}
