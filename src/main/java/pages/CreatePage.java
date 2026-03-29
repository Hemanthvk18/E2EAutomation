package pages;

import managers.TestContextManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class CreatePage {
    private static final Logger logger = LoggerFactory.getLogger(CreatePage.class);
    protected TestContextManager context;

    @FindBy(xpath = "//footer//button[@type='submit' and text()='Submit']")
    WebElement submitBtn;

    @FindBy(xpath = "//footer//button[@type='button' and text()='Cancel']")
    WebElement cancelBtn;

    public CreatePage(TestContextManager context) {
        this.context = context;
        PageFactory.initElements(context.getDriver(), this);

    }

    public abstract WebElement getCreateText();
}
