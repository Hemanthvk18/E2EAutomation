package pages;

import managers.TestContextManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CartPage {

    private static final Logger logger = LoggerFactory.getLogger(CartPage.class);
    TestContextManager context;

    public CartPage(TestContextManager context) {
        this.context = context;
        PageFactory.initElements(context.getDriver(), this);
    }

    @FindBy(xpath = "//button[contains(normalize-space(),'Continue Shopping')]")
    WebElement continueShoppingButton;

    @FindBy(xpath = "//button[contains(normalize-space(),'Checkout')]")
    WebElement checkoutButton;

    public boolean proceedWithCheckout() {
       return context.getCustomActions().customClick(checkoutButton,"Checkout Button");
    }

    public boolean verifyProductsPresence(String productName) {
        String path = String.format("//h3[contains(normalize-space(),'%s')]", productName.trim());
        context.getCustomWait().waitForElementToBeVisible(By.xpath(path), productName);
        return context.getCustomActions().isElementPresent(By.xpath(path));

    }

    public String getProductPrice(String productName) {
        String path = String.format("//h3[contains(normalize-space(.),'%s')]/parent::*//following-sibling::div/p", productName.trim());
        WebElement priceElement = context.getCustomWait().waitForElementToBeVisible(By.xpath(path), "Price for product: " + productName);
        return context.getCustomActions().getText(priceElement);
    }

    public String getProductStockStatus(String productName) {
        String path = String.format("//h3[contains(normalize-space(.),'%s')]//following-sibling::p[@class='stockStatus']", productName.trim());
        WebElement stockStatusElement = context.getCustomWait().waitForElementToBeVisible(By.xpath(path), "Stock status for product: " + productName);
        return context.getCustomActions().getText(stockStatusElement);
    }
}
