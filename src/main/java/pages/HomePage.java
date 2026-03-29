package pages;


import managers.TestContextManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HomePage {
    private static final Logger logger = LoggerFactory.getLogger(HomePage.class);
    TestContextManager context;

    public HomePage(TestContextManager context) {
        this.context = context;
        PageFactory.initElements(context.getDriver(), this);
    }

    @FindBy(xpath = "//*[@id='sidebar']//*[contains(normalize-space(.),'Home')]")
    WebElement homepage;

    @FindBy(xpath = "//section//input[@placeholder='search']")
    WebElement filterSearch;


    public boolean checkHomeVisible() {
        return context.getCustomActions().checkElementDisplayed(homepage, "Home page");
    }

    public boolean searchProduct(String productName) {
        context.getCustomActions().customSendKeys(filterSearch, "Product filter search", productName);
        return context.getCustomActions().pressEnter(filterSearch, "Product filter search");
    }

    public boolean validateSearchedProduct(String productName) {
        //changes made
        String xpath = "//h1[contains(normalize-space(.),'" + productName + "')]";
        WebElement searchedProduct = context.getCustomWait().waitForElementToBeVisible(By.xpath(xpath), "Searched product: " + productName);
        return context.getCustomActions().checkElementDisplayed(searchedProduct, "Searched product: " + productName);
    }

    public boolean validateActionButtonsForProduct(String productName, String buttonName) {
        String xpath = "//h5[contains(normalize-space(.),'" + productName + "')]//following-sibling::button[contains(normalize-space(.),'" + buttonName + "')]";
        WebElement actionButton = context.getCustomWait().waitForElementToBeClickable(By.xpath(xpath), buttonName + " button for product: " + productName);
        return context.getCustomActions().checkElementDisplayed(actionButton, buttonName + " button for product: " + productName);
    }


}
