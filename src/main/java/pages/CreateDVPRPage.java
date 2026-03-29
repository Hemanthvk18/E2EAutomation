package pages;


import managers.TestContextManager;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

public class CreateDVPRPage extends CreatePage {
    private static final Logger logger = LoggerFactory.getLogger(CreateDVPRPage.class);

    @FindBy(xpath = "//li[normalize-space (text()) = 'Create New DVPR']")
    WebElement createText;

    @FindBy(id = "dvprTypeId")
    WebElement dvprTypeDropdown;

    @FindBy(id = "techSpecNo")
    WebElement techSpecNo;

    @FindBy(id = "description")
    WebElement description;

    public CreateDVPRPage(TestContextManager context) {
        super(context);
    }

    @Override
    public WebElement getCreateText() {
        return createText;

    }
}
