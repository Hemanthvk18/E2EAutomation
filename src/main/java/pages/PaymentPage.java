package pages;

import managers.TestContextManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaymentPage {
    private static final Logger logger = LoggerFactory.getLogger(PaymentPage.class);
    TestContextManager context;

    public PaymentPage(TestContextManager context) {
        this.context = context;
        PageFactory.initElements(context.getDriver(), this);
    }

    @FindBy(xpath = "//input[@placeholder='Select Country']")
    WebElement selectCountryDropDown;

    public boolean selectCountry(String country){
        context.getCustomActions().customSendKeys(selectCountryDropDown,"Select Country",country);
        String path= String.format("//section/button//span[normalize-space(.)='%s']",country.trim());
        return context.getCustomActions().customClick(By.xpath(path),"Country dropdown value :"+country);
    }

}
