package pages;


import managers.TestContextManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


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

    @FindBy(xpath = "//*[@role='alert' and contains(normalize-space(),'Product Added To Cart')]")
    WebElement productAddedAlert;

    @FindBy(xpath = "//ul//button[contains(normalize-space(),'Cart')]")
    WebElement cartButton;

//    @FindBy(css = ".card .card-img-top")
//    WebElement productImages;

    By productImages = By.cssSelector(".card .card-img-top");

    @FindBy(xpath = "//img[@class='card-img-top']")
    List<WebElement> productImageList;

    public List<WebElement> getProductImageList() {
        return productImageList;
    }

    public boolean checkProductImagesVisible() {
        context.getCustomWait().waitForElementPresent(productImages, "Product images in homepage");
        return context.getCustomActions().isElementPresent(productImages);
    }

    public WebElement getCartButtonWebElement() {
        return cartButton;
    }

    public boolean checkProductAddedAlertVisible() {
        return context.getCustomActions().checkElementDisplayed(productAddedAlert, "Product Added To Cart alert");
    }

    public void waitForProductAddedAlertToDisappear() {
        context.getCustomActions().waitForAlertMessageToDisappear("Product Added To Cart alert");
    }

    public WebElement addToCartButtonForProduct(String productName) {
        String xpath = "//h5[contains(normalize-space(.),'" + productName + "')]//following-sibling::button[contains(normalize-space(.),'Add To Cart')]";
        return context.getCustomWait().waitForElementToBeClickable(By.xpath(xpath), "Add to Cart button for product: " + productName);
    }

    public boolean checkHomeVisible() {
        return context.getCustomActions().checkElementDisplayed(homepage, "Home page");
    }

    public boolean searchProduct(String productName) {
        context.getCustomActions().customSendKeys(filterSearch, "Product filter search", productName);
        return context.getCustomActions().pressEnter(filterSearch, "Product filter search");
    }

    public boolean validateSearchedProduct(String productName) {
        //changes made h5
        String xpath = "//h5[contains(normalize-space(.),'" + productName + "')]";
        WebElement searchedProduct = context.getCustomWait().waitForElementToBeVisible(By.xpath(xpath), "Searched product: " + productName);
        return context.getCustomActions().checkElementDisplayed(searchedProduct, "Searched product: " + productName);
    }

    public boolean validateActionButtonsForProduct(String productName, String buttonName) {
        String xpath = "//h5[contains(normalize-space(.),'" + productName + "')]//following-sibling::button[contains(normalize-space(.),'" + buttonName + "')]";
        WebElement actionButton = context.getCustomWait().waitForElementToBeClickable(By.xpath(xpath), buttonName + " button for product: " + productName);
        return context.getCustomActions().checkElementDisplayed(actionButton, buttonName + " button for product: " + productName);
    }


}
