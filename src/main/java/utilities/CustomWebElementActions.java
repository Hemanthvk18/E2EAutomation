package utilities;

import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openqa.selenium.interactions.WheelInput;

import javax.swing.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CustomWebElementActions {
    private static final Logger logger = LoggerFactory.getLogger(CustomWebElementActions.class);
    WebDriverWait wait;
    CustomWait customWait;
    Actions actions;
    JavascriptExecutor js;
    WebDriver driver;

    public CustomWebElementActions(WebDriver driver, CustomWait customWait, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
        this.customWait = customWait;
        this.actions = new Actions(driver);
        this.js = (JavascriptExecutor) driver;
    }

    private static boolean verifyLink(String url) {
        try {
            URL link = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) link.openConnection();
            httpURLConnection.setConnectTimeout(3000); // Set connection timeout to 3 seconds
            httpURLConnection.connect();
            if (httpURLConnection.getResponseCode() == 200) {
                return true;
            } else {
                logger.error(url + " - " + httpURLConnection.getResponseMessage() + "-" + "is a broken link");
            }
        } catch (Exception e) {
            logger.error(url + " - " + "is a broken link");
        }
        return false;
    }

    private static void tryHover(WebDriver d, By loc) {
        try {
            new Actions(d).moveToElement(d.findElement(loc)).perform();
        } catch (NoSuchElementException ignored) {
        }
    }


    private static void tryClick(WebDriver d, By loc) {
        for (int i = 0; i < 3; i++) {
            try {
                d.findElement(loc).click();
                return;
            } catch (Exception e) {
                try {
                    Thread.sleep(150);
                } catch (InterruptedException ignored) {
                }
            }
        }

        throw new NoSuchElementException("Export menu item not found:" + loc);
    }

    private static String signature(Map<String, String> row, Set<String> headers) {
        StringBuilder sb = new StringBuilder();
        for (String h : headers) {
            String col = normalizeHeader(h);
            sb.append(col).append('=').append(n(row.get(col))).append('|');
        }
        return sb.toString();
    }

    private static String normalizeHeader(String s) {
        if (s == null) return "";
        String h = s.trim().replaceAll("\\s+", " ");
        return h.replaceAll("\\.$", ""); // "Part No." -> "Part No"
    }

    private static String normalizeCell(String v) {
        return v == null ? "" : v.trim().replaceAll("\\s+", " ");
    }

    private static String n(String s) {
        return s == null ? "" : s.trim();
    }

    private static long asLong(Object o) {
        return ((Number) o).longValue();
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }

    public void scrollIntoView(WebElement element) {
        wait.until(ExpectedConditions.visibilityOf(element));
        try {
            js.executeScript("arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});", element);
        } catch (JavascriptException e) {
            // Fallback for browsers or environments that don't support the options
            // parameter
            js.executeScript("arguments[0].scrollIntoView(true);", element);

        }

    }

    public void scrollIntoView(By elementLocator) {
        // wait until(ExpectedConditions.visibilityOfElementLocated (elementLocator));
        try {
            js.executeScript("arguments[0].scrollIntoView((block: 'center', inline: 'nearest'});",
                    driver.findElement(elementLocator));
        } catch (JavascriptException e) {
            // Fallback for browsers or environments that don't support the options
            // parameter
            js.executeScript("arguments[0].scrollIntoView(true);", driver.findElement(elementLocator));
        }
    }


    public void dragAndDrop(By source, By destination) {
        actions.dragAndDrop(driver.findElement(source), driver.findElement(destination)).build().perform();
    }

    public void scrollToBottom(By viewPortLocator) {
        // js.executeScript("window.scrollTo(e, document.body.scrollHeight);");
        WebElement viewport = driver.findElement(viewPortLocator);
        long lastScrollHeight = -1;
        long currentScrollHeight = (long) js.executeScript("return arguments[0].scrollHeight", viewport);
        while (lastScrollHeight != currentScrollHeight) {
            lastScrollHeight = currentScrollHeight;
            js.executeScript("arguments[0].scrollTo(0, arguments[0].scrollHeight)", viewport);
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        currentScrollHeight = (long) js.executeScript("return arguments[0].scrollHeight", viewport);
    }

    public void scrollToTop(By scrollElementLocator) {
        WebElement scrollableTableElement = driver.findElement(scrollElementLocator);

        js.executeScript("arguments[0].scrollTop = 0;", scrollableTableElement);
    }


    public void moveToElement(WebElement element, String elementName) {
        try {
            actions.moveToElement(element).build().perform();
        } catch (Exception e) {
            logger.error("An error occured while trying to move to element.");
        }
    }

    public void moveToElement(By locator, String elementName) {
        try {
            WebElement element = getElementWithCustomLocator(locator);
            if (element != null) {
                actions.moveToElement(driver.findElement(locator)).build().perform();
            } else {
                logger.error("Could not find element with locator:" + locator);
            }
        } catch (Exception e) {
            logger.error("An error occured while trying to move to element.");
        }
    }

    public void genericInputValue(WebElement element, String input) {
        String elementType = element.getTagName().toLowerCase();
        // For text input fields and textareas
        if (elementType.equals("textarea")) {
            customSendKeys(element, elementType, input);
        } else if (elementType.equals("input") || elementType.equals("date")) {
            String type = element.getAttribute("type");
            if (type.equals("text")) {
                customSendKeys(element, elementType, input);
            } else if (type.equals("checkbox")) {
                clickCheckbox(element, elementType);
            } else if (type.equals("radio") && element.isSelected()) {
                clickRadioBtn(element, elementType);
            } else if (elementType.equals("select")) {
                selectOptionFromSelectDropdownByText(element, elementType, input);
            } else {
                System.out.println("Element of type" + elementType + "does not have a 'clear' or 'reset' action.");
            }
        }
    }


    public boolean doubleClick(WebElement element, String elementName) {
        try {
            WebElement visibleElement = customWait.waitForElementToBeVisible(element, elementName);
            WebElement clickableElement = customWait.waitForElementToBeClickable(element, elementName);

            if (visibleElement != null && clickableElement != null) {
                logger.info("Double Clicking on {}.", elementName);
                actions.moveToElement(element);
                actions.doubleClick(element).build().perform();
                return true;

            }
        } catch (Exception e) {
            logger.error("Failed to double click on {} element", elementName);
        }
        return false;
    }

    public boolean doubleClick(By locator, String elementName) {
        try {
            WebElement visibleElement = customWait.waitForElementToBeVisible(driver.findElement(locator), elementName);
            WebElement clickableElement = customWait.waitForElementToBeClickable(driver.findElement(locator), elementName);

            if (visibleElement != null && clickableElement != null) {
                logger.info("Double Clicking on {}.", elementName);
                actions.moveToElement(driver.findElement(locator));
                actions.doubleClick(driver.findElement(locator)).build().perform();
                return true;
            }

        } catch (Exception e) {
            logger.error("Failed to double click on {} element", elementName);
        }
        return false;
    }


    public boolean customSendKeysTypeEachCharacter(WebElement element, String elementName, String inputValue) {
        if (inputValue == null || inputValue.trim().isEmpty()) {
            logger.error("Invalid input value for {}.", elementName);
            throw new IllegalArgumentException("Input value cannot be null or empty.");
        }
        try {
            customClick(element, elementName);
            clearAndTypeEachCharacter(element, elementName, inputValue);
            wait.until(ExpectedConditions.textToBePresentInElementValue(element, inputValue));
            return true;

        } catch (Exception e) {
            logger.error("Error while entering value into input field:{}", elementName, e.getMessage());
        }

        return false;
    }


    public boolean customSendKeysTypeEachCharacter(By elementLocator, String elementName, String inputValue) {
        if (inputValue == null || inputValue.trim().isEmpty()) {
            logger.error("Invalid input value for {}.", elementName);
            throw new IllegalArgumentException("Input value cannot be null or empty.");
        }
        try {
            customClick(elementLocator, elementName);
            clearAndTypeEachCharacter(elementLocator, elementName, inputValue);
            wait.until(ExpectedConditions.textToBePresentInElementValue(elementLocator, inputValue));
            return true;

        } catch (Exception e) {
            logger.error("Error while entering value into input field:{}", elementName, e.getMessage());
        }

        return false;
    }

    //-----------------------------------------------------------------------------------------

    public boolean customSendKeys(WebElement element, String elementName, String inputValue) {

        if (inputValue == null || inputValue.trim().isEmpty()) {
            logger.error("Invalid input value for {}.", elementName);
            throw new IllegalArgumentException("Input value cannot be null or empty.");
        }
        try {
            customClick(element, elementName);
            clearAndType(element, elementName, inputValue);
            wait.until(ExpectedConditions.textToBePresentInElementValue(element, inputValue));
            return true;
        } catch (Exception e) {
            logger.error("Error while entering value into input field: {}", elementName, e.getMessage());
        }
        return false;
    }

    public boolean customSendKeys(By element, String elementName, String inputValue) {

        if (inputValue == null || inputValue.trim().isEmpty()) {
            logger.error("Invalid input value for {}.", elementName);
            throw new IllegalArgumentException("Input value cannot be null or empty.");
        }
        try {
            customClick(element, elementName);
            clearAndType(element, elementName, inputValue);
            wait.until(ExpectedConditions.textToBePresentInElementValue(element, inputValue));
            return true;
        } catch (Exception e) {
            logger.error("Error while entering value into input field: {}", elementName, e.getMessage());
        }
        return false;
    }


    public boolean customSendKeys(String elementName, String inputValue) {

        if (inputValue == null || inputValue.trim().isEmpty()) {
            logger.error("Invalid input value for ().", elementName);

            throw new IllegalArgumentException("Input value cannot be null or empty.");

        }

        try {
            // Normalize label for case-insensitive matching
            String normalizedLabel = elementName.toLowerCase();

            // XPath with and without validator
            By locator = By.xpath(
                    "//*[contains(translate (normalize-space(text()), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'" + normalizedLabel + ")]" +
                            "//ancestor-or-self::validator//input[@type='text']" +
                            " | " +
                            "//*[contains (translate(normalize-space(text()), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnoparstuvwxyz'),'" + normalizedLabel + "')]" +
                            "//ancestor-or-self::validator//textarea" +
                            " | " +
                            "//*[contains (translate (normalize-space(text()), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + normalizedLabel + "')]" +
                            "//following::input[@type='text'][1]" +
                            " | " +
                            "//*[contains(translate (normalize-space(text()), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + normalizedLabel + "')]" +
                            "//following::input[@type='text'][1]" +
                            "//following::textarea[1]"
            );

            logger.info("Trying to locate input field using XPath: {}", locator.toString());
            customClick(locator, elementName);
            clearAndType(locator, elementName, inputValue);
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.textToBePresentInElementValue(locator, inputValue),
                    ExpectedConditions.textToBePresentInElementLocated(locator, inputValue)
            ));
            return true;
        } catch (Exception e) {
            logger.error("Error while entering value into input field: {}", elementName, e);
        }
        return false;
    }

    public void typeNoVerify(WebElement element, String elementName, String inputValue) {
        if (inputValue == null || inputValue.trim().isEmpty()) {
            throw new IllegalArgumentException("Input value cannot be null or empty for" + elementName + ".");
        }
        // Fast path: click, select all, type; no value-equals wait
        customClick(element, elementName);
        element.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        element.sendKeys(inputValue);
    }

    public boolean isElementPresent(By locator) {
        try {
            return driver.findElements(locator).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isElementDisplayed(By locator) {
        try {
            customWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean customClick(WebElement element, String name) {
        return performClick(() -> customWait.waitForElementToBeClickable(element, name), name);
    }

    public boolean customClick(By locator, String name) {
        return performClick(() -> customWait.waitForElementToBeClickable(locator, name), name);
    }

//Internal reusable click logic

    private boolean performClick(Supplier<WebElement> supplier, String name) {

        try {

            // 1) First clickable attempt

            WebElement el = supplier.get();
            if (el == null) {
                logger.error("clickable element {} not found.", name);
                return false;

            }
            scrollIntoView(el);

            // Try 1: Standard click
            try {
                logger.info("Clicking {} using standard click", name);
                el.click();
                return true;
            } catch (StaleElementReferenceException |
                     ElementNotInteractableException e) {
                logger.warn("Standard click failed on {} with {}. Retrying...", name, e.getClass().getSimpleName());
            }

            // 2) Retry: fresh element
            el = supplier.get();
            if (el == null) {
                logger.error("Retry failed: clickable {} not found.", name);
                return false;
            }
            scrollIntoView(el);

            // Try 2: Actions click
            try {
                logger.info("Attempting Actions click on {}", name);
                actions.moveToElement(el).pause(Duration.ofMillis(50)).click().perform();
                return true;

            } catch (ElementClickInterceptedException e) {
                logger.warn("Actions click intercepted on {}. Falling back to JS click.", name);

            } catch (Exception e) {
                logger.warn("Actions click failed on {} with {}. Trying JS click.", name, e.getClass().getSimpleName());

            }


            // Try 3: JS click
            js.executeScript("arguments[0].click();", el);

            logger.info("JS click succeeded on {}", name);
            return true;
        } catch (TimeoutException e) {
            logger.error("Timeout waiting for {} to be clickable: {}", name, e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error clicking {}: {}", name, e.getMessage());

        }
        return false;
    }


    public boolean clearAndType(WebElement element, String elementName, String inputValue) {
        try {
            WebElement visibleElement = customWait.waitForElementToBeVisible(element, elementName);
            if (visibleElement != null) {
                logger.info("Typing into {}.", elementName);
                scrollIntoView(visibleElement);
                visibleElement.clear();
                visibleElement.sendKeys(inputValue);
                return true;
            }
        } catch (Exception e) {
            logger.error("Failed to type into {}. Error: {}", elementName, e.getMessage());
        }
        return false;
    }


    public boolean clearAndType(By locator, String elementName, String inputValue) {
        try {
            WebElement visibleElement = customWait.waitForElementToBeVisible(locator, elementName);
            if (visibleElement != null) {
                logger.info("Typing into {}.", elementName);
                scrollIntoView(visibleElement);
                visibleElement.clear();
                visibleElement.sendKeys(inputValue);
                return true;
            }
        } catch (Exception e) {
            logger.error("Failed to type into {}. Error: {}", elementName, e.getMessage());
        }
        return false;
    }

    public boolean dragAndDropBy(WebElement source, int xOffset, int yOffset, String sourceName) {
        try {
            WebElement s = customWait.waitForElementToBeVisible(source, sourceName);
            if (s == null) return false;
            scrollIntoView(s);
            actions.dragAndDropBy(s, xOffset, yOffset).perform();
            logger.info("Dragged {} by offsets x:{} y:{}", sourceName, xOffset, yOffset);
            return true;
        } catch (Exception e) {
            logger.error("dragAndDropBy failed on {}: {}", sourceName, e.getMessage());
            return false;
        }
    }

    public boolean dragAndDrop(WebElement source, WebElement target, String sourceName, String targetName) {
        try {
            WebElement s = customWait.waitForElementToBeVisible(source, sourceName);
            WebElement t = customWait.waitForElementToBeVisible(target, targetName);
            if (s == null || t == null) return false;
            scrollIntoView(s);
            scrollIntoView(t);
            actions.dragAndDrop(s, t).perform();
            logger.info("Dragged {} to {}", sourceName, targetName);
            return true;
        } catch (Exception e) {
            logger.error("dragAndDrop failed from {} to {}: {}", sourceName, targetName, e.getMessage());
            return false;
        }
    }


    public void loadPageWithRetry(String url, By element, int maxRetries) {
        int attempts = 0;
        while (attempts < maxRetries) {
            try {
                driver.get(url);
                wait.until(ExpectedConditions.visibilityOfElementLocated(element));
                logger.info("URL:{} loaded successfully.", url);
                return;
            } catch (Exception e) {
                logger.warn("Attempt " + (attempts + 1) + " of trying to load URL failed. Retrying...");
                attempts++;
                if (attempts < maxRetries) {
                    logger.info("Refreshing the page for url:" + url);
                    // driver.navigate().refresh()
                    js.executeScript("location.reload()");
                }
            }
        }
        throw new RuntimeException("Failed to load the page after " + maxRetries + " attempts.");
    }


    public WebElement getElementWithText(String elementText) {

        By locator = By.xpath("//*[translate(normalize-space(text()), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz') =*"
                + elementText.toLowerCase() + "]");

        customWait.waitForElementToBeVisible(locator, "Element with Text:" + elementText);
        return getElementWithCustomLocator(locator);
    }

    public boolean clickElementWithText(String elementText, String elementType) {
        try {
            String xpath = buildFlexibleXpath(elementText, elementType);
            By locator = By.xpath(xpath);
            WebElement visibleElement = customWait.waitForElementToBeVisible(locator, elementType);
            WebElement clickableElement = customWait.waitForElementToBeClickable(locator, elementType);
            if (visibleElement != null && clickableElement != null) {
                return customClick(clickableElement, elementText + " " + elementType);
            }


        } catch (NoSuchElementException e) {
            logger.error("Element not found: {} with text '{}'", elementType, elementText, e);
            throw new RuntimeException("Element not found: " + elementType + " with text '" + elementText + "'", e);

        } catch (Exception e) {
            logger.error("Unexpected error while clicking {} '{}'", elementType, elementText, e);
            throw new RuntimeException("Failed to click " + elementType + " with text '" + elementText + "'", e);
        }

        return false;
    }


    public WebElement getElementWithCustomLocator(By locator) {
        try {
            WebElement element = driver.findElement(locator);
            return element;
        } catch (NoSuchElementException e) {
            logger.error("Element not found for locator: {}", locator);
            return null;
        }
    }

    public String buildFlexibleXpath(String elementText, String elementType) {
        String escapedText = elementText.replace("'", "\\'");

        switch (elementType.toLowerCase()) {
            case "button":
                // Match <button> with text, even if it contains <span> or other children
                return String.format("(//button[normalize-space(.)='%s'])[last()]", escapedText);
            case "span":
                return String.format("(//span[normalize-space(text())='%s'])[last()]", escapedText);
            default:
                // Fallback to any element with matching visible text
                return String.format("(//*[normalize-space(.)='%s']) [last()]", escapedText);
        }
    }


    public String getUrl() {
        String url = null;
        if (js.executeScript("return document.readyState").equals("complete")) {
            url = driver.getCurrentUrl();
        }
        return url;
    }

    public boolean waitForPageLoad() {
        return wait.until(driver -> js.executeScript("return document.readyState").equals("complete"));
    }


    public void clearAndTypeEachCharacter(WebElement element, String elementName, String inputValue) {
        logger.info("Clearing {} values.", elementName);
        // element.clear();
        js.executeScript("arguments[0].value='';", element);
        for (char c : inputValue.toCharArray()) {
            element.sendKeys(String.valueOf(c));
            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
        logger.info("Entered data into {} field.", elementName);
    }


    public void clearAndTypeEachCharacter(By elementLocator, String elementName, String inputValue) {
        logger.info("Clearing {} values.", elementName);
        // element.clear();
        WebElement element = driver.findElement(elementLocator);
        js.executeScript("arguments[0].value='';", element);
        for (char c : inputValue.toCharArray()) {
            element.sendKeys(String.valueOf(c));
            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
        logger.info("Entered data into () field.", elementName);

    }

    public void clearTextBox(WebElement element, String elementName) {
        logger.info("Clearing values in {).", elementName);
        // Clear the field by sending the Backspace key multiple times
        element.sendKeys(Keys.CONTROL + "a"); // Select all text (optional)
        element.sendKeys(Keys.BACK_SPACE); // Press Backspace key
    }


    public void clearTextBox(By locator, String elementName) {
        logger.info("Clearing {} values in.", elementName);
        // Clear the field by sending the Backspace key multiple times
        driver.findElement(locator).sendKeys(Keys.CONTROL + "a"); // Select all text (optional)
        driver.findElement(locator).sendKeys(Keys.BACK_SPACE); // Press Backspace key
    }

    public void genericClearValue(WebElement element) {
        String elementType = element.getTagName().toLowerCase();
        element.click();
        // For text input fields and textareas
        if (elementType.equals("input") || elementType.equals("textarea")) {
            String type = element.getAttribute("type");
            if (type.equals("text") || type.equals("password") || type.equals("email") || type.equals("search")
                    || type.equals("tel") || type.equals("url")) {
                try {
                    clearTextBox(element, elementType);
                } catch (Exception ignored) {
                } //1) Try native clear
                try {
                    element.clear();
                } catch (Exception ignored) {
                }

                // 2) CTRL + A + DELETE
                element.sendKeys(Keys.chord(Keys.CONTROL, "a"));
                element.sendKeys(Keys.DELETE);
                if (!isEmptyValue(element)) {
                    element.sendKeys(Keys.BACK_SPACE);
                }

                //3) JS fallback for React/Angular controlled inputs }
                if (!isEmptyValue(element)) {
                    jsSetInputValue(element, "");
                }
                return;

            } else if (type.equals("checkbox") && element.isSelected()) {
                element.click(); // Uncheck the checkbox
            } else if (type.equals("radio") && element.isSelected()) {
                element.click(); // Deselect the radio button
            }

        } else if (elementType.equals("select")) {
            Select select = new Select(element);
            select.deselectAll(); // Deselect all options (if multi-select dropdown)

        } else {
            System.out.println("Element of type" + elementType + " does not have a 'clear' or 'reset' action.");
        }
    }

    private boolean isEmptyValue(WebElement el) {
        String v = el.getAttribute("value");
        return v == null || v.isEmpty();

    }


    private void jsSetInputValue(WebElement el, String value) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(
                "const e arguments[0], val arguments[1];" +
                        "let protoe.constructor === HTMLTextAreaElement? HTML TextAreaElement.prototype HTMLInputElement.prototype;" +
                        "const setter Object.getOwnPropertyDescriptor (proto, 'value').set;" +
                        "setter.call(e, val);" +
                        "e.dispatchEvent(new Event('input', (bubbles:true}));" +
                        "e.dispatchEvent(new Event('change', {bubbles: true)));" +
                        "e.blur();",

                el, value
        );
    }

    private void jsClearContentEditable(WebElement el) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(
                "const e arguments[0];" +

                        "e.innerHTML=''; e.textContent='';" +
                        "e.dispatchEvent(new Event('input', {bubbles:true}));" +
                        "e.dispatchEvent(new Event('change', {bubbles: true)));" +
                        "e.blur();",
                el
        );
    }

    private void safeFocus(WebElement el) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].focus();", el);
    }

    public boolean checkElementDisplayed(WebElement element, String elementName) {
        WebElement visibleElement = customWait.waitForElementToBeVisible(element, elementName, Duration.ofSeconds(15));
        if (visibleElement != null) {
            logger.info("{} is visible.", elementName);
            return true;
        }
        return false;
    }

    public boolean checkElementDisplayed(By locator, String elementName) {

        boolean visible = customWait.waitForElementToBeVisible(locator, elementName) != null; // uses locator-based visibility
        if (visible) {
            logger.info("{} is visible.", elementName);
        } else {
            logger.info("{} is not visible within the timeout.", elementName);
        }
        return visible;
    }


    public String getAttribute(By locator, String value) {
        return driver.findElement(locator).getAttribute(value);
    }

    public String getAttribute(WebElement element, String value) {
        return element.getAttribute(value);
    }

    public String getText(By locator) {
        return driver.findElement(locator).getText();
    }

    public String getText(WebElement element) {
        return element.getText();
    }


    public WebElement getButtonWithText(String buttonText) {
        By locator = By.xpath("//button [normalize-space(.)='" + buttonText + "']");
        customWait.waitForElementToBeVisible(locator, buttonText + "Button");
        return driver.findElement(locator);
    }

    public boolean checkButtonDisabledWithButtonText(String buttonText, String buttonName) {
        return isDisabled(getButtonWithText(buttonText));

    }

    public boolean isDisabled(WebElement element) {
        // 1) DOM boolean attribute
        String disabledDom = element.getDomAttribute("disabled");
        if (disabledDom != null) {
            return true;
        }

        // 2) Classic attribute presence
        String disabledAttr = element.getAttribute("disabled");
        if (disabledAttr != null) {
            return true;
        }

        // 3) ARIA semantics
        String ariaDisabled = element.getAttribute("aria-disabled");
        if ("true".equalsIgnoreCase(ariaDisabled)) {
            return true;
        }

        // 4) Heuristic CSS class
        String cls = element.getAttribute("class");
        if (cls != null && cls.toLowerCase().contains("disabled")) {
            return true;
        }

        // 5) Interactive state as last resort
        return !element.isEnabled();
    }

    public boolean checkButtonEnabledWithButtonText(String buttonText, String buttonName) {
        WebElement button = getButtonWithText(buttonText);
        if (button.isEnabled()) {
            logger.info("{} is visible.", buttonName);
            return true;
        }
        return false;
    }

    public String getTextOfElementWithCustomLocator(By locator) {
        try {
            WebElement element = getElementWithCustomLocator(locator);
            if (element != null) {
                return element.getText().trim();
            } else {
                logger.error("Element is null for locator: {}", locator);
                return null;
            }
        } catch (NoSuchElementException e) {
            logger.error("Element not found for locator: {}", locator);
            return null;
        } catch (Exception e) {
            logger.error("Error occurred while fetching text for locator: {}", locator, e);
            return null;

        }
    }

    public WebElement getElementWithLabelAndAncestor(String label, String parentTagName, String elementToGet) {
        By locator = By.xpath("//*[contains (normalize-space(text()),'" + label + "')]//ancestor-or-self::"
                + parentTagName + "//" + elementToGet);
        return getElementWithCustomLocator(locator);
    }


    public By getLocatorWithLabelAndAncestor(String label, String parentTagName, String elementToGet) {
        By locator = By.xpath("//*[contains(normalize-space(text())," + label + "')]//ancestor-or-self::"
                + parentTagName + "//" + elementToGet);
        return locator;
    }

    public String getTextOfElementWithLabel(String label, String parentTagName, String elementToGet) {
        return getElementWithLabelAndAncestor(label, parentTagName, elementToGet).getText();

    }

    public boolean checkElementNotDisplayed(By locator, String elementName) {

        boolean invisibleOrAbsent = customWait.waitForElementToBeInvisible(locator, elementName); // uses locator-based invisibility
        if (invisibleOrAbsent) {
            logger.info("{} is not visible / not present.", elementName);
        } else {
            logger.info("{} is visible.", elementName);
        }
        return invisibleOrAbsent;
    }


    public boolean checkElementNotDisplayed(WebElement locator, String elementName) {
        boolean invisibleOrAbsent = customWait.waitForElementToBeInvisible(locator, elementName); // uses locator-based invisibility
        if (invisibleOrAbsent) {
            logger.info("{} is not visible / not present.", elementName);
        } else {
            logger.info("{} is visible.", elementName);
        }
        return invisibleOrAbsent;
    }

    public boolean checkUrlContainsText(String text) {
        if (wait.until(ExpectedConditions.urlContains(text))) {
            return true;
        }
        return false;
    }

//    public void loadPageWithRetry(String url, WebElement element, int maxRetries) {
//        int attempts = 0;
//        while (attempts < maxRetries) {
//            try {
//                driver.get(url);
//                wait.until(ExpectedConditions.visibilityOf(element));
//                logger.info("URL:{} loaded successfully.", url);
//                return;
//            } catch (Exception e) {
//                logger.warn("Attempt" + (attempts + 1) + "of trying to load URL failed.Retrying...");
//                attempts++;
//                if (attempts < maxRetries) {
//                    logger.info("Refreshing the page for url:" + url);
//                    // driver.navigate().refresh(
//                    js.executeScript("location.reload()");
//                }
//
//            }
//
//        }
//
//        throw new RuntimeException("Failed to load the page after " + maxRetries + " attempts.");
//    }

    public void loadPageWithRetryUntilTextAppears(String url, String text, int maxRetries) {
        int attempts = 0;
        while (attempts < maxRetries) {
            try {
                driver.get(url);
                wait.until(ExpectedConditions.urlContains(text));
                logger.info("URL: {} loaded successfully.", url);
                return;
            } catch (Exception e) {
                logger.warn("Attempt" + (attempts + 1) + " of trying to load URL failed. Retrying...");
                attempts++;
                if (attempts < maxRetries) {
                    logger.info("Refreshing the page for url:" + url);
                    // driver.navigate().refresh(
                    js.executeScript("location.reload()");
                }
            }
        }
        throw new RuntimeException("Failed to load the page after " + maxRetries + " attempts.");
    }

//
//    public boolean waitForPageLoad() {
//        return wait.until(driver -> js.executeScript("return document.readyState").equals("complete"));
//    }

    public boolean clickDropdown(WebElement dropdownElement, String elementName) {
        return customClick(dropdownElement, elementName + "Dropdown");
    }

    public boolean clickDropdown(By locator, String elementName) {
        return customClick(locator, elementName + "Dropdown");
    }

    public void deselectLiCheckboxValues(WebElement dropDownElement, String dropdownName,
                                         List<WebElement> dropdownValues, WebElement selectAllButton) {
        try {
            logger.info("Deselecting values from dropdown {}", dropdownName);
            for (WebElement dropdownValue : dropdownValues) {
//                if (!selectAllButton.isDisplayed()) {
//                    logger.info("Dropdown has collapsed. Clicking on the dropdown again to desecelt values.");
//                    customClick(dropDownElement, dropdownName);// if select all is not visible it means the dropdown has
//                    // collapsed-if so cl dropdown again
//                }

                if (dropdownValue.getAttribute("class").contains("p-highlight")) {
                    dropdownValue.click();
                    logger.info("Deselected value {} from dropdown {}", dropdownValue.getText(), dropdownName);
                }
            }

        } catch (Exception e) {
            logger.error("Error while trying to deselect values from {} dropdown", dropdownName);
            e.printStackTrace();
            throw e;
        }
    }

    public void deselectLiCheckboxValues(By dropdownLocator, String dropdownName, By dropdownValuesLocator) {
        try {
            logger.info("Deselecting values from dropdown {}", dropdownName);
            customWait.waitForAllElementsToBeVisible(dropdownValuesLocator);
            for (WebElement dropdownValue : driver.findElements(dropdownValuesLocator)) {
                if (dropdownValue.getAttribute("class").contains("p-highlight")) {
                    dropdownValue.click();
                    logger.info("Deselected value {} from dropdown {}", dropdownValue.getText(), dropdownName);
                }
            }
        } catch (Exception e) {
            logger.error("Error while trying to deselect values from {} dropdown", dropdownName);
            e.printStackTrace();
            throw e;
        }
    }

    public void verifyLiCheckboxValues(WebElement dropDownElement, String dropdownName, List<WebElement> dropdownValues, String selectedValuesString, String delimiter) {

        // If selectedValuesString is not null, split it into a list of selected values
        List<String> selectedValues = (selectedValuesString != null)
                ? Arrays.asList(selectedValuesString.split(delimiter))
                : new ArrayList<>();
        try {
            // Click the dropdown to expand it
            clickDropdown(dropDownElement, dropdownName);
            logger.info("Checking selected values from dropdown {}", dropdownName);

            // Create a set from the selected values to eliminate duplicates and ensure fast
            //lookup
            Set<String> selectedValuesSet = new HashSet<>(selectedValues);

            // If no selected values, handle the case for an empty selection scenario
            if (selectedValuesSet.isEmpty()) {
                if (dropdownValues.size() == 1
                        && "No results found".equalsIgnoreCase(dropdownValues.get(0).getText().trim())) {
                    logger.info("No values present in {} dropdown.", dropdownName);
                    return;
                }

                // Ensure no values are erroneously selected if not expected
                for (WebElement dropdownValue : dropdownValues) {
                    if (dropdownValue.getAttribute("class").contains("p-highlight")) {
                        logger.error("No value was expected to be selected, but value {} is selected in dropdown {}.", dropdownValue.getText(), dropdownName);
                        throw new RuntimeException("No value was expected to be selected, but value" + dropdownValue.getText() + " is selected in dropdown" + dropdownName);
                    }
                }
            }

            // Check each selected value in the dropdown

            for (String selectedValue : selectedValuesSet) {
                boolean found = false;
                for (WebElement dropdownValue : dropdownValues) {
                    if (dropdownValue.getText().trim().equalsIgnoreCase(selectedValue)) {
                        found = true;
                        // If the value is found, check if it is highlighted (selected)
                        if (dropdownValue.getAttribute("class").contains("p-highlight")) {
                            logger.info("Value {} is selected in dropdown {} as expected.", selectedValue,
                                    dropdownName);
                        } else {
                            logger.error("Expected value {} not selected in dropdown {}.", selectedValue, dropdownName);
                            throw new RuntimeException("Expected value" + selectedValue + " was not selected in dropdown " + dropdownName + ".");
                        }

                        break; // Exit the loop once the selected value is found
                    }
                }

                // If the selected value is not found in the dropdown, log and throw an error
                if (!found) {
                    logger.error("Expected value {} not present in dropdown {}.", selectedValue, dropdownName);
                    throw new RuntimeException(
                            "Expected value '" + selectedValue + "' was not present in dropdown" + dropdownName + ".");
                }
            }
        } catch (Exception e) {
            logger.error("Error while trying to check selected values from {} dropdown", dropdownName);
            e.printStackTrace();
            throw e;

        } finally {
            // Ensure that the dropdown is closed at the end of the process
            clickDropdown(dropDownElement, dropdownName);
        }
    }


    public void deselectMatCheckboxValues(WebElement dropDownElement, String dropdownName,
                                          List<WebElement> dropdownValues) {
        try {
            logger.info("Deselecting values from dropdown {}", dropdownName);
            for (WebElement dropdownValue : dropdownValues) {
                js.executeScript("arguments[0].scrollIntoview({block: 'center', inline: 'nearest'));", dropdownValue);
                if (dropdownValue.getAttribute("class").contains("checkbox-checked")) {
                    logger.info("Deselecting dropdown item: {}", dropdownValue.getText());
                    dropdownValue.click();
                    logger.info("Deselected {) from dropdown {}.", dropdownValue.getText(), dropdownName);
                }
            }
        } catch (Exception e) {
            logger.error("Error while trying to deselect values from {} dropdown", dropdownName);
            throw e;
        }
    }

    public boolean selectLiCheckboxValues(WebElement dropDownElement, String elementName, List<String> valuesToSelect,
                                          List<WebElement> dropdownValues) {
        Set<String> valuesSet = new HashSet<>(valuesToSelect);
        Map<String, WebElement> dropdownMap = new HashMap<>();
        for (WebElement dropdownValue : dropdownValues) {
            dropdownMap.put(dropdownValue.getAttribute("aria-label").trim(), dropdownValue);
        }
        boolean allSelected = true;
        for (String value : valuesSet) {
            if (!dropdownMap.containsKey(value)) {
                throw new RuntimeException("Could not find value " + value + "in dropdown" + elementName);
            }
            WebElement dropdownValue = dropdownMap.get(value);
            try {
                boolean isValueSelected = dropdownValue.getAttribute("class").contains("p-highlight");
                if (dropdownValue != null && dropdownValue.isEnabled() && !isValueSelected) {
                    customClick(dropdownValue, "Dropdown value: " + value);
                } else {
                    allSelected = false;
                }
            } catch (IndexOutOfBoundsException e) {
                logger.error("Value" + value + " not present in dropdown" + elementName);
            }
        }
        return allSelected;
    }

    public boolean selectLiCheckboxValues(By dropdownElementLocator, String dropdownName, String valuesToSelect) {
        try {
            if (getElementWithCustomLocator(dropdownElementLocator) != null) {
                boolean isValueSelected = getAttribute(dropdownElementLocator, "class").contains("p-highlight");
                try {
                    // If it's not selected and enabled, click on it
                    if (!isValueSelected && getElementWithCustomLocator(dropdownElementLocator).isEnabled()) {
                        customClick(dropdownElementLocator, "Dropdown value:" + valuesToSelect);
                        return true;
                    } else if (isValueSelected) {
                        logger.info("Value '{}' is already selected.", valuesToSelect);
                        return true;
                    } else {
                    }
                } catch (Exception e) {
                    logger.error("Error while processing value '{}': {}", valuesToSelect, e.getMessage());
                }

            } else {
                logger.warn("Value '{}' not found in dropdown {}'.", valuesToSelect, dropdownName);
                throw new RuntimeException(
                        "Value '" + valuesToSelect + "not found in dropdown" + dropdownName + ".");
            }

        } catch (Exception e) {
            logger.error("Error selecting values from dropdown '{}': {}", dropdownName, e.getMessage());
        }
        return false;
    }

    public boolean selectMatCheckboxValues(WebElement dropDownElement, String
                                                   elementName, List<String> valuesToSelect,
                                           List<WebElement> dropdownValues) {
        boolean allSelected = true;
        for (String value : valuesToSelect) {
            if (value != null && (value.isEmpty())) {
                boolean valueFound = false;
                for (WebElement option : dropdownValues) {
                    js.executeScript("arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});", option);
                    String label = option.findElement(By.tagName("label")).getText();
                    if (label.equalsIgnoreCase(value)) {
                        valueFound = true;
                        boolean isSelected = option.getAttribute("class").contains("checked");
                        if (!isSelected) {
                            customClick(option, label);
                        }
                        break;
                    }
                }
                if (!valueFound) {
                    logger.error("Value" + value + " not present in dropdown" + elementName);
                    allSelected = false;
                    throw new RuntimeException("Value" + value + " not present in dropdown" + elementName);
                }
            }
        }
        return allSelected;
    }


    public void selectMultipleMatCheckboxes(String elementName, List<String> labels, By optionLocator) {

//        if (csvLabels == null || csv csvLabels.trim().isEmpty()) return;
//
// Split by comma, trim, ignore blanks, dedupe while preserving order
//        LinkedHashSet<String> labels= Arrays.stream(csvLabels.split(","))
//                .map(String:: trim)
//                .filter(s -> Is.isEmpty())
//                .collect(Collectors.toCollection (LinkedHashSet::new));

        for (String label : labels) {
            selectMatCheckboxIfNeeded(elementName, label, optionLocator);
        }

    }

    public void selectMatCheckboxIfNeeded(String elementName, String labelToSelect, By optionLocator) {

        for (WebElement option : driver.findElements(optionLocator)) {
            try {
                String label = option.findElement(By.tagName("label")).getText().trim();
                if (label.equalsIgnoreCase(labelToSelect)) {
                    boolean isSelected = isMatCheckboxChecked(option);
                    if (!isSelected) {
                        js.executeScript("arguments[0].scrollIntoView({block: 'center', inline: 'nearest'));", option);
                        customClick(option, label);
                    }
                    return;
                }
            } catch (NoSuchElementException | StaleElementReferenceException ignored) {
                logger.error("Could not select Environment {}", labelToSelect);
            }

        }
        logger.warn("Checkbox '{}' not found under '{}'", labelToSelect, elementName);
    }

    public boolean selectAutocompleteMultiCheckDropDownValues(WebElement element, String dropdownElementName,

                                                              WebElement textbox, List<String> valuesToSelect, List<WebElement> dropdownValues, WebElement

                                                                      closeBtn) {
        boolean allSelected = false;
//        if (valuesToSelect.size() == 0 && valuesToSelect.get(0).
//                equalsIgnoreCase("Select All")) {
//            customClick(selectAllBtn, "Select All checkbox for "dropdownElementName);
//            return true;
//        }
        for (String value : valuesToSelect) {
            value = value.trim();
            if (value != null && !(value.isEmpty())) {
                customSendKeys(textbox, "Autocomplete Textbox for" + dropdownElementName, value);
                boolean valueFound = false;
                try {
                    wait.until(ExpectedConditions.visibilityOfAllElements(dropdownValues));
                    for (WebElement option : dropdownValues) {
                        String optionText = option.getAttribute("aria-label").trim();
                        if (optionText.equalsIgnoreCase(value)) {
                            valueFound = true;
                            boolean isValueSelected = option.getAttribute("class").contains("p-highlight");
                            if (!isValueSelected) {
                                customClick(option, "Dropdown value" + option.getText());
                                allSelected = true;
                            }
                            break;
                        }
                    }
                } catch (Exception e) {
                    logger.error("An error occured while selecting vlaue: " + value + "dropdown" + dropdownElementName);
                    allSelected = false;
                }
                if (!valueFound) {
                    logger.error("Value" + value + "not present in dropdown" + dropdownElementName);
                    allSelected = false;
                    throw new IllegalArgumentException(
                            "Could not find the value" + value + " in dropdwon " + dropdownElementName);
                }

            } else {
                allSelected = false;
                throw new NullPointerException("Value to select is null for + dropdownElementName + dropdown.");
            }
        }

        customClick(closeBtn, "Dropdown close button");

        return allSelected;
    }

    public boolean selectAutocompleteMultiCheckDropDownValues(By optionsBy,
                                                              List<String> valuesToSelect,
                                                              String dropdownName,
                                                              By textbox,
                                                              By closeBtn) {

        if (valuesToSelect == null || valuesToSelect.isEmpty()) {
            throw new IllegalArgumentException("No values provided for " + dropdownName + "dropdown.");
        }

        boolean allSelected = true;
        List<String> notFound = new ArrayList<>();
        customWait.waitForAllElementsToBePresent(By.cssSelector("p-multiselectitem li"));
        for (String raw : valuesToSelect) {
            String value = (raw == null) ? "" : raw.trim();
            if (value.isEmpty()) {
                throw new IllegalArgumentException("A value to select is null/empty for " + dropdownName + "dropdown.");
            }

            // 1) Filter the list for this value
            customSendKeys(textbox, "Autocomplete Textbox for " + dropdownName, value);

            // 2) Wait for the specific option to appear (by aria-label == value)
            WebElement option = customWait.waitForOptionByAriaLabel(optionsBy, value);
            if (option == null) {
                logger.error("Value '{}' not present in dropdown {}", value, dropdownName);
                allSelected = false;
                notFound.add(value);
                continue;
            }


            // 3) Click only if not already selected
            if (!hasClass(option, "p-highlight")) {
                customClick(option, "Dropdown value " + value + " '");

                //4) Confirm selection after potential re-render (re-find by aria-label)
                customWait.waitForCondition(d -> {
                    WebElement refreshed = safeWaitForOption(optionsBy, value);
                    return refreshed != null && hasClass(refreshed, "p-highlight");
                }, "Confirm '" + value + "marked as selected");
            }
        }

        // 5) Close dropdown (if your UI requires)
        try {
            customClick(closeBtn, "Dropdown close button");
        } catch (Exception ignored) {
        }
        if (!notFound.isEmpty()) {
            throw new IllegalArgumentException(
                    "Could not find/select in " + dropdownName + ":" + String.join(", ", notFound)
            );
        }

        return allSelected;
    }


    private WebElement safeWaitForOption(By optionsBy, String value) {
        try {
            return customWait.waitForOptionByAriaLabel(optionsBy, value);
        } catch (Exception ignore) {
            return null;
        }

    }

    private boolean hasClass(WebElement el, String klass) {
        try {
            String cls = el.getAttribute("class");
            if (cls == null) return false;
            for (String token : cls.split("\\s+")) if (klass.equals(token)) return true;
        } catch (StaleElementReferenceException ignored) {
        }
        return false;
    }

    public boolean selectAllInMultiSelect(By selectAllBy, String dropdownName, By closeBtn) {
        customClick(selectAllBy, "Select All for " + dropdownName);
        try {
            customClick(closeBtn, "Dropdown close button");
        } catch (Exception ignored) {
        }
        return true;
    }


    public boolean selectAutocompleteMultiCheckDropDownValues(String dropdownElementName, String valuesToSelect, String seperator) {

        WebElement parentDropdownSection = driver.findElement(By.xpath(
                "//*[contains (translate (normalize-space(text()), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '"
                        + dropdownElementName.toLowerCase().trim()
                        + "')]//ancestor-or-self:: validator//p-autocomplete | //*[contains (translate (normalize-space(text()), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '"
                        + dropdownElementName.toLowerCase().trim()
                        + "')]//ancestor-or-self::div[contains(@class, 'fieldset-description')]"));

        WebElement dropdown = parentDropdownSection.findElement(By.xpath(".//p-multiselect"));
        customClick(dropdown, dropdownElementName);
        WebElement inputBox = dropdown
                .findElement(By.xpath(".//descendant-or-self::input[contains(@class,'p-multiselect-filter')]"));
        List<String> valuesToSelectList = Arrays.asList(valuesToSelect.split(seperator));
        for (String valueToSelect : valuesToSelectList) {
            valueToSelect = valueToSelect.trim();
            if (valueToSelect != null && !(valueToSelect.isEmpty())) {
                customSendKeys(inputBox, "Autocomplete Textbox for " + dropdownElementName, valueToSelect);
                customWait.waitForAllElementsToBeVisible(
                        dropdown.findElements(By.xpath(".//descendant-or-self::p-multiselectitem")));
                try {
                    WebElement dropdownElementToSelect = dropdown
                            .findElement(By.xpath(".//descendant-or-self:: p-multiselectitem/li[contains(@aria-label,'"
                                    + valueToSelect + "')]"));

                    boolean isValueSelected = dropdownElementToSelect.getAttribute("class").contains("p-highlight");
                    if (!isValueSelected) {
                        customClick(dropdownElementToSelect, " Dropdown value " + dropdownElementToSelect.getText());
                        return true;
                    }
                } catch (NoSuchElementException e) {
                    logger.error("Could not find value" + valueToSelect + " in " + dropdownElementName + " dropdown.");

                    throw new RuntimeException(

                            "Could not find value " + valueToSelect + " in " + dropdownElementName + " dropdown.");
                }
            } else {
                logger.error("Value to select is null");
            }
        }
        customClick(dropdown, dropdownElementName);

        return false;

    }

    public boolean selectAutocompleteDropDownValue(String dropdownElementName, String valueToSelect) {
        // Normalize search parameters once
        String normalizedElementName = dropdownElementName.toLowerCase().trim();
        String normalizedValue = valueToSelect.trim();
        if (normalizedValue == null || normalizedValue.isEmpty()) {
            throw new IllegalArgumentException("Value to select is null for:" + dropdownElementName);
        }
        // Create locator for parent section (simplified XPath)
        String parentXpath = String.format(
                "//*[contains(translate(normalize-space(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '%s')]"
                        + "/ancestor::*[self::validator or contains (@class, 'fieldset-description')]",
                normalizedElementName);

        try {
            // Wait for parent section to be present
            WebElement parentSection = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(parentXpath)));
            scrollIntoView(parentSection);
            // Locate input within parent section
            WebElement input = parentSection.findElement(By.xpath(".//input"));
            // Type value with debounce wait
            customSendKeysTypeEachCharacter(input, dropdownElementName + "AutoComplete", normalizedValue);

            // Wait for options to appear and find target
            By optionsLocator = By
                    .xpath("//li[contains(@class, 'p-autocomplete-item') and not(contains (@class, 'p-disabled'))]"
                            + "/span[contains(normalize-space(),'" + normalizedValue + "')]");

            customWait.waitForAllElementsToBePresent(optionsLocator);
            WebElement targetOption = wait.until(ExpectedConditions.visibilityOfElementLocated(optionsLocator));

            // Select the option
            return customClick(targetOption, "Dropdown Value: " + normalizedValue);
        } catch (TimeoutException e) {
            logger.error("Dropdown '{}' or value '{}' not found. XPath: {}", dropdownElementName, normalizedValue,
                    parentXpath);

        } catch (Exception e) {
            logger.error("Error selecting '{}' from '{}': {}", normalizedValue, dropdownElementName, e);

// Keyboard fallback
            try {
                WebElement active = driver.switchTo().activeElement();
                active.sendKeys(Keys.BACK_SPACE);
                active.sendKeys(Keys.ARROW_DOWN);
                active.sendKeys(Keys.ENTER);
                return true;
            } catch (Exception kbEx) {
                logger.warn("Keyboard fallback failed for '{}': {}", dropdownElementName, kbEx.getMessage());
            }
        }
        return false;
    }


    public boolean selectAutocompleteDropDownValue(WebElement dropdownElement, String dropdownElementName,
                                                   String valueToSelect) {
        customSendKeysTypeEachCharacter(dropdownElement, dropdownElementName + "AutoComplete Textbox for", valueToSelect);
        try {

            By optionExact = By.xpath("//ul[@role='listbox']//li[@role='option' and normalize-space(.)='" + valueToSelect + "']");
            customWait.waitForElementToBeClickable(optionExact, "Autocomplete option '" + valueToSelect + " '", Duration.ofSeconds(5));
            WebElement opt = driver.findElement(optionExact);
            return customClick(opt, "Dropdown value " + valueToSelect);
        } catch (IndexOutOfBoundsException e) {
            logger.error("Value {} not found in dropdown {}.", valueToSelect, dropdownElementName);
        }
        return false;
    }

    public boolean selectAutocompleteDropDownValue(By dropdownLocator, By dropdownValueLocator, String dropdownElementName, String valueToSelect) {
        customSendKeysTypeEachCharacter(dropdownLocator, "AutoComplete Textbox for " + dropdownElementName, valueToSelect);
        customWait.waitForElementToBeVisible(dropdownValueLocator, valueToSelect);
        return customClick(dropdownValueLocator, valueToSelect);
    }

    public boolean verifyDropdownOptions(By dropdownLocator, By optionLocator, List<String> expectedOptions,
                                         String dropdownName) {
        try {
            // Open the dropdown
            clickDropdown(dropdownLocator, dropdownName);

            // Wait for options to be visible
            List<WebElement> optionElements = customWait.waitForAllElementsToBeVisible(optionLocator);
            List<String> actualOptions = optionElements.stream().map(WebElement::getText).map(String::trim).collect(Collectors.toList());

            // Use your existing utility method for comparison
            return CustomStringUtils.compareListOfStringElements(expectedOptions, actualOptions, "Dropdown Options",
                    dropdownName, false);

        } catch (Exception e) {
            logger.error("Error verifying dropdown options for {}: {}", dropdownName, e.getMessage());
            return false;
        }
    }


    public boolean verifyPMultiselectDropdownText(List<String> valuesToSelect, WebElement dropdown,
                                                  List<WebElement> dropdownItems) {
        String dropdownText = dropdown.getText().trim();
        int dropdownSize = dropdownItems.size();
        int valuesSize = valuesToSelect.size();
        if (dropdownSize == 1 && valuesSize == 1 && dropdownText.equals(valuesToSelect.get(0))) {
            logger.info("Dropdown Text is '{}' as expected.", valuesToSelect.get(0));
            return true; // Correct behavior for a single selection with single value in dropdown
        }
        if (valuesToSelect.size() == 1 && dropdownText.equals(valuesToSelect.get(0))) {
            logger.info("Dropdown Text is '{}' as expected.", valuesToSelect.get(0));
            return true; // Correct behavior for a single selection displaying the value selected
        }

        if (valuesToSelect.size() > 1) {
            String expectedText = valuesSize + " items selected";
            if (dropdownText.equals(expectedText)) {
                logger.info("Dropdown Text is '{}' as expected.", valuesSize);
                return true; // Correct behavior for multiple selections showing the count

            }
        }

        return false;
    }

    // need refinement to check the values in the dropdown
    public boolean verifyMatCheckBoxDropdownText(List<String> valuesToSelect, WebElement dropdown, List<WebElement> dropdownItems) {
        String dropdownText = dropdown.getText().trim();
        int dropdownSize = dropdownItems.size();
        int valuesSize = valuesToSelect.size();
        if (dropdownSize == 1 && valuesSize == 1 && dropdownText.equals("All")) {
            logger.info("Dropdown Text is 'All' as expected.");
            return true; // Correct behavior for a single selection with single value in dropdown
        }
        if (valuesToSelect.size() == 1 && dropdownText.equals(valuesToSelect.get(0))) {
            logger.info("Dropdown Text is '{}' as expected.", valuesToSelect.get(0));
            return true; // Correct behavior for a single selection displaying the value selected
        }

        // Case 3: If multiple values are selected, the text should display the count of
        // selected items
        if (valuesToSelect.size() > 1) {
            String expectedText = valuesSize + " items selected";
            if (dropdownText.equals(expectedText)) {
                logger.info("Dropdown Text is '{}' as expected.", valuesSize);
                return true; // Correct behavior for multiple selections showing the count
            }
        }
        return false;
    }


    public boolean inputIntoFilter(String columnName, String inputValue) {
        try {
            WebElement filter = driver.findElement(By.xpath("(//input[@aria-label='" + columnName + " Filter Input']) [last()]"));
            scrollIntoView(filter);
            if (customSendKeys(filter, "Filter for " + columnName, inputValue.trim())) {
                return true;
            }
        } catch (Exception e) {
            logger.error("An error occured while filtering column {} with input {}", columnName, inputValue, e.getMessage());
        }
        return false;
    }

    public boolean clearFilterValues(String columnName) {
        try {
            WebElement filter = driver
                    .findElement(By.xpath("(//input[@aria-label='" + columnName + " Filter Input']) [last()]"));
            clearTextBox(filter, "Filter column " + columnName);
        } catch (Exception e) {
            logger.error("An error occured while trying to clear values in Filter column {}. Cause: {}", columnName,
                    e.getMessage());
        }
        return false;
    }

    private void handleSort(By columnLocator, boolean targetAscending) {
        String targetSort = targetAscending ? "ascending" : "descending";
        String currentSort = getAttribute(columnLocator, "aria-sort");
        if (targetSort.equalsIgnoreCase(currentSort)) {
            logger.info("Already in {} sort", targetSort);
            return;
        }

        if ("none".equalsIgnoreCase(currentSort)) {
            customClick(columnLocator, "Set initial sort");
        } else if (targetAscending && "descending".equalsIgnoreCase(currentSort)) {
            // Descending Ascending requires 2 clicks
            customClick(columnLocator, "Clear descending sort");
            customClick(columnLocator, "Set ascending sort");
        } else {
            // Ascending Descending requires 1 click
            customClick(columnLocator, "Set descending sort");
        }

        String sortValueAfterClick = getAttribute(columnLocator, "aria-sort");
        if (sortValueAfterClick.equalsIgnoreCase(targetSort)) {
            logger.info("Column sorted in " + targetSort.toUpperCase() + " order.");
        }
    }


    public boolean sortColumnAndValidate(String columnName, boolean ascendingOrDescending) {
        boolean sortColumnResult = false;
        if (isColumnFound(By.tagName("ag-grid-angular"), columnName)) {
            sortColumnResult = sortColumn(columnName, ascendingOrDescending);
        }
        return sortColumnResult;
    }

//    public List<String> filterColumnAndValidate(String columnName, String filterValue) {
//        List<String> mismatches = new ArrayList<String>();
//        try {
//            if (isColumnFound(By.tagName("ag-grid-angular"), columnName)) {
//                inputIntoFilter(columnName, filterValue);
////                waitForGridToFinishLoading();
//                String colId = getColumnIndexByHeader(columnName);
//                List<String> cellValues = getTextFromWebElementListWithLocator(colId);
//                if (cellValues.isEmpty()) {
//                    logger.info("Filter returnred no results for '{}'", filterValue);
//                    return mismatches;
//                }
//                for (String cellText : cellValues) {
//                    if (!cellText.toLowerCase().contains(filterValue.toLowerCase())) {
//                        mismatches.add("Unexpected value:" + cellText);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            logger.error("Error while trying to filter and validate column {}. Error:{}", columnName, e.getMessage());
//        } finally {
//            clearFilterValues(columnName);
//        }
//        return mismatches;
//
//    }


//    public List<String> validateFilteredColumn(String columnName, String filterValue) {
//        List<String> mismatches = new ArrayList<String>();

    /// /        waitForGridToFinishLoading();
//        String colId = getColumnIndexByHeader(columnName);
//        List<String> cellValues = getTextFromWebElementListWithLocator(colId);
//        for (int i = 0; i < cellValues.size(); i++) {
//            String cellText = cellValues.get(i);
//            if (!cellText.toLowerCase().contains(filterValue.toLowerCase())) {
//                mismatches.add("Row" + (i + 1) + ":" + cellText + "'");
//            }
//        }
//        return mismatches;
//    }
    public String getColumnIndexByHeader(String columnName) {
        By columnLocator = By.xpath("//span[@ref='eText' and normalize-space(text())='" + columnName + "']//ancestor-or-self::div[@role='columnheader']");
        String colId = getAttribute(columnLocator, "col-id");
        return colId;
    }


    public boolean saveView(String viewName) {
        try {
            clickElementWithText("Save View", "Save View Button");
            if (checkDialogBoxWithHeaderDisplayed("Create New View")) {
                customSendKeys(driver.findElement(By.id("filter")), "Save View View Name Textbox", viewName);
                return clickElementWithText("Create", "Create View Button");
            }
        } catch (Exception e) {
            logger.error("An error occured while trying to save view. Cause:{}", e.getMessage());
        }
        return false;
    }

    public String getCurrentFilterValue(String columnName) {
        String value = "";
        try {
            WebElement filter = driver.findElement(By.xpath("(//input[@aria-label='" + columnName + " Filter Input']) [last())"));
            scrollIntoView(filter);
            value = filter.getAttribute("value");
        } catch (Exception e) {
            logger.error("An error occured while checking if column {} filter is empty. Error:{}", columnName,
                    e.getMessage());
        }
        return value;
    }


    public boolean selectFromPdropdown(By dropdownLocator, String dropdownNme, String valueToSelect) {
        // By dropdownLocator By.xpath("//p-dropdown[@optionlabel='" + dropdownOptionlabel + "]");
        customWait.waitForElementToBeVisible(dropdownLocator, dropdownNme);
        customClick(dropdownLocator, dropdownNme);
        By valueToSelectInDropdownLocator = By.xpath("//p-dropdownitem//*[normalize-space(text())='" + valueToSelect + "']");
        customWait.waitForElementToBeVisible(valueToSelectInDropdownLocator,
                dropdownNme + "dropdown value" + valueToSelect);
        customClick(valueToSelectInDropdownLocator, dropdownNme + "dropdown value" + valueToSelect);
        String selectedDropdownValue = getTextOfElementWithCustomLocator(dropdownLocator).trim();

        //getCurrentSelectedPdropdownValue(dropdownOptionlabel);
        if (selectedDropdownValue != null && selectedDropdownValue.equalsIgnoreCase(selectedDropdownValue)) {
            return true;
        } else {
            logger.error("Could not select value () from dropdown {).", valueToSelect, dropdownNme);
        }
        return false;
    }


    public String getCurrentSelectedPdropdownValue(String dropdownOptionlabel) {
        By locator = By.xpath(
                "//p-dropdown[@optionlabel='" + dropdownOptionlabel + "']" +
                        "//span[contains(@class, 'p-dropdown-label')]"
        );
        return getTextOfElementWithCustomLocator(locator).trim();
    }

    public void filterWithSingleCondition(String columnName, String condition, String filterValue) {

        WebElement filterMenuButton = driver.findElement(By.xpath("(//input[@aria-label='" + columnName
                + "Filter Input'])[last()]/ancestor-or-self::div[@role='gridcell']//button[@aria-label='Open Filter Menu']"));
        WebElement filterOperator = driver
                .findElement(By.xpath("//div[not(contains(@class, 'hidden')) and contains(@class, 'ag-select')]"));
    }


    public boolean isColumnFound(By gridRoot, String headerTextExact) {
        WebElement root = getElementWithCustomLocator(gridRoot);
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // 1) Header viewport (for labels visibility)
        WebElement headerViewport = null;
        List<By> headerCandidates = Arrays.asList(
                By.cssSelector(".ag-header-viewport, [ref='eHeaderViewport']"),
                By.cssSelector(".ag-header-container, [ref='eHeaderContainer']"),
                By.cssSelector(".ag-header")

        );

        for (By c : headerCandidates) {
            List<WebElement> found = root.findElements(c);

            if (!found.isEmpty()) {
                headerViewport = found.get(0);
                break;
            }

        }
        if (headerViewport == null) {
            throw new RuntimeException("AG Grid header viewport not found");
        }

        //--- 2) True horizontal scroller (BODY scroller only)
        WebElement viewport = null;
        List<By> bodyCandidates = Arrays.asList(
                By.cssSelector(".ag-center-cols-viewport, [ref='eCenterViewport'], [ref='eViewport']"),
                By.cssSelector(".ag-body-viewport, [ref='eBodyViewport']"),
                By.cssSelector(".ag-body-horizontal-scroll-viewport, [ref=' eBodyHorizontalScrollViewport ']")
        );

        for (By c : bodyCandidates) {
            List<WebElement> els = root.findElements(c);
            for (WebElement el : els) {
                Number cw = (Number) js.executeScript("return arguments[0].clientWidth;", el);
                Number sw = (Number) js.executeScript("return arguments[e].scrollWidth;", el);

                String ox = String.valueOf(js.executeScript("return getComputedStyle(arguments[0]).overflowX;", el));
                boolean scrollable = sw.intValue() > cw.intValue() && !"hidden".equalsIgnoreCase(ox);

                if (scrollable) {
                    viewport = el;
                    break;
                }
            }
            if (viewport != null) break;
        }
        if (viewport == null) {
            List<WebElement> fallback =
                    root.findElements(By.cssSelector(".ag-center-cols-viewport, [ref='eCenterViewport'], [ref='eViewport']"));
            if (!fallback.isEmpty()) viewport = fallback.get(0);
            else throw new RuntimeException("No AG Grid horizontal scroller found");
        }


        //---3) Label locator within header viewport
        By headerLabelSelector = By.cssSelector(".ag-header-cell-label.ag-header-cell-text, [ref='eLabel'] [ref='eText'], ag-header-cell-text");

        // 4) Already visible?
        if (isHeaderTextVisibleInHeaderViewport(headerViewport, headerLabelSelector, headerTextExact)) {
            return true;
        }

        //--- 5) Scroll metrics T
        int clientWidth = ((Number) js.executeScript("return arguments[0].clientWidth;", viewport)).intValue();
        int scrollWidth = ((Number) js.executeScript("return arguments[0].scrollWidth;", viewport)).intValue();
        int maxScroll = Math.max(0, scrollWidth - clientWidth);
        int step = Math.max(24, (int) Math.round(clientWidth * 0.85));

        // Start from current position (in case grid was already scrolled)
        int pos = ((Number) js.executeScript("return Math.round(arguments[0].scrollLeft);", viewport)).intValue();

        //---6) Scan forward (to the right)
        for (int i = 0; i < 80 && pos <= maxScroll; i++) {
            pos = Math.min(maxScroll, pos + step);
            js.executeScript("arguments[0].scrollLeft = Math.round(arguments[1]);", viewport, pos);

            // allow header to sync
            sleep(5);

            if (isHeaderTextVisibleInHeaderViewport(headerViewport, headerLabelSelector, headerTextExact)) {
                return true;
            }
        }

        //---7) If not found, scan backward (to the left)
        for (int i = 0; i < 80 && pos >= 0; i++) {

            pos = Math.max(0, pos - step);
            js.executeScript("arguments[0].scrollLeft = Math.round(arguments[1]);", viewport, pos);
            // allow header to sync
            sleep(5);
            if (isHeaderTextVisibleInHeaderViewport(headerViewport, headerLabelSelector, headerTextExact)) {
                return true;

            }
        }

        //---8) Final reset to 0; keeps header/body aligned (we only scrolled the body scroller)
        js.executeScript("arguments[0].scrollLeft = 0;", viewport);
        sleep(5);
        return false;
    }


    private boolean isHeaderTextVisibleInHeaderViewport(WebElement headerViewport, By headerLabelSelector, String headerTextExact) {
        List<WebElement> labels = headerViewport.findElements(headerLabelSelector);
        for (WebElement l : labels) {
            String t = l.getText();
            if (t != null && t.trim().equals(headerTextExact)) {
                if (isHorizontallyVisible(headerViewport, l)) return true;
            }
        }
        return false;
    }

    public List<String> getAllColumnNamesFromTable(WebElement tableParent, By columnLocator) {
        List<String> columnNames = new ArrayList<>();
        Map<Integer, String> columnData = new TreeMap<>();
        WebElement scrollableTableElement = tableParent.findElement(By.xpath(".//div[@ref='eViewport'][1]"));

        try {
            // Check if the table has a horizontal scrollbar
            long scrollWidth = (Long) js.executeScript("return arguments[0].scrollWidth;", scrollableTableElement);
            long clientWidth = (Long) js.executeScript("return arguments[0] .clientWidth;", scrollableTableElement);

            if (scrollWidth > clientWidth) {
                // Scrollable table logic
                long scrollLeft = 0;

                while (scrollLeft < scrollWidth) {
                    // Fetch visible column headers

                    List<WebElement> headers = tableParent.findElements(columnLocator);

                    for (WebElement header : headers) {
                        WebElement ancestorDiv = header.
                                findElement(By.xpath(".//ancestor-or-self::div[@role='columnheader']"));


                        // Get the aria-rowindex attribute value
                        String ariaColIndex = ancestorDiv.getAttribute("aria-colindex");

                        String columnName = header.getText().trim();

                        if (!columnName.isEmpty() && !columnData.containsValue(columnName)) {
                            scrollIntoView(header);

                            // addHighlighToElement(header, columnName);
                            columnData.put(Integer.parseInt(ariaColIndex), columnName);

                        }
                    }

                    // Scroll further

                    scrollLeft += clientWidth;
                    js.executeScript("arguments[0].scrollLeft = arguments[0].scrollLeft + arguments[0].clientWidth;",
                            scrollableTableElement);
                    Thread.sleep(500); // Pause for loading
                }

                // Optional: reset scrollbar to left if needed
                js.executeScript("arguments[0].scrollLeft = 0;", scrollableTableElement);
                columnNames.addAll(columnData.values());
            } else {
                // Non-scrollable table logic
                List<WebElement> headers = tableParent.findElements(columnLocator);
                for (WebElement header : headers) {
                    String columnName = header.getText().trim();
                    if (!columnName.isEmpty() && !columnNames.contains(columnName)) {
                        columnNames.add(columnName);
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error occurred during scrolling", e);

        }
        return columnNames;
    }


    private boolean isHorizontallyVisible(WebElement viewport, WebElement el) {
        String js = "var vb arguments[0].getBoundingClientRect();" +
                "var b = arguments[1].getBoundingClientRect();" +
                "return (b.right > vb.left && b.left < vb.right);";
        return Boolean.TRUE.equals(((JavascriptExecutor) driver).
                executeScript(js, viewport, el));
    }

    public void dragAndDropColumns(By tableParentLocator, String columnNameToPin, By destinationLocator) {
        WebElement tableParent = driver.findElement(tableParentLocator);
        WebElement scrollableTableElement = tableParent.findElement(By.xpath(".//div[@ref='eViewport'][1]"));
        By columnLocator = By.xpath(".//span[@ref='eText']");
        try {
            // Check if the table has a horizontal scrollbar
            long scrollWidth = (Long) js.executeScript("return arguments[0].scrollWidth;", scrollableTableElement);
            long clientWidth = (Long) js.executeScript("return arguments[0].clientWidth;", scrollableTableElement);

            if (scrollWidth > clientWidth) {
                // Scrollable table logic
                long scrollLeft = 0;
                int retires = 0;
                int maxRetires = 5;

                while (scrollLeft < scrollWidth && retires++ < maxRetires) {
                    // Fetch visible column headers
                    List<WebElement> headers = tableParent.findElements(columnLocator);
                    for (WebElement header : headers) {

                        String columnNameFromGrid = header.getText().trim();
                        if (!columnNameFromGrid.isEmpty() && columnNameFromGrid.equalsIgnoreCase(columnNameToPin)) {
                            scrollIntoView(header);
                            addHighlighToElement(header, columnNameFromGrid);
                            actions.dragAndDrop(header, driver.findElement(destinationLocator)).build().perform();
                            return;
                        }
                    }

                    // Scroll further
                    scrollLeft += clientWidth;
                    js.executeScript("arguments[0].scrollLeft = arguments[0].scrollLeft + arguments[0].clientWidth;",
                            scrollableTableElement);
                    Thread.sleep(500); // Pause for loading
                }
                throw new RuntimeException("Column" + columnNameToPin + " not found after scrolling");
            } else {
                // Non-scrollable table logic
                By columnToDrag = By.xpath("//span[@ref='eText' and text()='" + columnNameToPin
                        + "]/ancestor-or-self::div[@role='columnheader']");
                dragAndDrop(columnToDrag, destinationLocator);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error occurred during scrolling", e);
        } finally {
            // Optional: reset scrollbar to left if needed
            js.executeScript("arguments[0].scrollLeft = 8;", scrollableTableElement);
        }

    }


    public void resetScrollBarToLeft(WebElement scrollableTableElement) {
        js.executeScript("arguments[0].scrollLeft = 0;", scrollableTableElement);
    }


    public void resetScrollBarToLeft(By tableParent) {
        WebElement scrollableTableElement = driver
                .findElement(By.xpath("(//div[contains(@class, 'ag-center-cols-viewport')])[last()]"));
        js.executeScript("arguments[0].scrollLeft;", scrollableTableElement);
    }


    public LinkedHashMap<String, String> getAllColumnNameWithColIdFromTable(WebElement tableParent, By columnLocator) {
        LinkedHashMap<String, String> columnNamesAndId = new LinkedHashMap<>();
        WebElement scrollableTableElement = tableParent.findElement(By.xpath(".//div[@ref='eViewport'][1]"));

        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;

            // Reset scrollbar
            js.executeScript("arguments[0].scrollLeft=0;", scrollableTableElement);
            Thread.sleep(500);

            long scrollWidth = (long) js.executeScript("return arguments[0].scrollWidth;", scrollableTableElement);
            long clientWidth = (long) js.executeScript("return arguments[0].clientWidth;", scrollableTableElement);
            boolean isScrollable = scrollWidth > clientWidth;

            int maxScrollAttempts = 20;
            long lastScrollLeft = -1;
            int scrollAttempt = 0;

            while (isScrollable && scrollAttempt < maxScrollAttempts) {
                List<WebElement> headers = tableParent.findElements(columnLocator);
                for (WebElement header : headers) {
                    String columnName = header.getText().trim();
                    String colId = header.getAttribute("col-id");
                    if (!columnName.isEmpty() && colId != null && !colId.isEmpty()
                            && !columnNamesAndId.containsKey(columnName)) {
                        columnNamesAndId.put(columnName, colId);

                    }

                }
                long currentScrollLeft = (long) js.executeScript("return arguments[0].scrollLeft;",
                        scrollableTableElement);
                if (currentScrollLeft == lastScrollLeft) {
                    break; // No more scrolling
                }

                lastScrollLeft = currentScrollLeft;
                js.executeScript("arguments[0].scrollLeft = arguments[0].scrollLeft + arguments[0].clientWidth 8.9;", scrollableTableElement);
                scrollAttempt++;
                Thread.sleep(500); // Allow DOM to update
            }

// If not scrollable, read directly

            if (!isScrollable) {
                List<WebElement> headers = tableParent.findElements(columnLocator);
                for (WebElement header : headers) {
                    String columnName = header.getText().trim();
                    String colId = header.getAttribute("col-id");
                    if (!columnName.isEmpty() && colId != null && !colId.isEmpty()) {
                        columnNamesAndId.put(columnName, colId);
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            throw new RuntimeException("Error occurred during scrolling", e);
        } finally {
            // Optional: reset scrollbar to left if needed
            js.executeScript("arguments[0].scrollLeft 0;", scrollableTableElement);
        }
        return columnNamesAndId;
    }

    public boolean selectOptionFromSelectDropdownByText(String dropdownName, String valueToSelect) {
        try {
            if (valueToSelect == null || valueToSelect.isBlank()) {
                throw new NullPointerException("Value to select is null for dropdown" + dropdownName);
            }

            Select selectDropdown = new Select(driver.findElement(By.xpath(
                    "//*[contains(translate(normalize-space(text()), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '"
                            + dropdownName.toLowerCase().trim()
                            + "')]//ancestor-or-self::validator//select | //*[contains (translate (normalize-space(text()), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '"
                            + dropdownName.toLowerCase().trim()
                            + "')]//ancestor-or-self::div[contains(@class, 'fieldset-description')]//select")));

            selectDropdown.selectByVisibleText(valueToSelect.trim());
            WebElement selectedOption = selectDropdown.getFirstSelectedOption();
            String selectedText = selectedOption.getText();
            if (valueToSelect.equals(selectedText)) {
                return true;
            }
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException(
                    "Could not find the specified value" + valueToSelect + "in" + dropdownName + "dropdown.");
        }
        return false;
    }


    public boolean clickCheckbox(WebElement checkbox, String checkboxForElement) {
        // scrollIntoView(checkbox);
        boolean isSelected = isCheckBoxChecked(checkbox);
        if (!isSelected) {
            checkbox.click();
            if (isCheckBoxChecked(checkbox)) {
                logger.info("Checkbox for {} selected.", checkboxForElement);
                return true;
            } else {
                logger.info("Checkbox for {} could not be selected.", checkboxForElement);
            }

        } else {
            logger.info("Checkbox for {} already selected.", checkboxForElement);
        }
        return false;

    }


    private boolean isMatCheckboxChecked(WebElement host) {
        String cls = host.getAttribute("class");
        return cls != null && (cls.contains("mat-mdc-checkbox-checked") || cls.contains("mat-checkbox-checked"));
    }

    public boolean isCheckBoxChecked(WebElement checkbox) {
        String ariaLabel = checkbox.getAttribute("aria-label");
        String classAttr = checkbox.getAttribute("class");

        boolean isSelected = (ariaLabel != null && ariaLabel.contains("(checked)"))
                || (classAttr != null && classAttr.contains("selected"));
        return isSelected;
    }

    public boolean isCheckBoxChecked(By checkbox) {
        String ariaLabel = getAttribute(checkbox, "aria-label");
        String classAttr = getAttribute(checkbox, "class");
        boolean isSelected = (ariaLabel != null && ariaLabel.contains("(checked)"))
                || (classAttr != null && classAttr.contains("selected"));

        return isSelected;
    }

    public boolean clickCheckbox(By checkboxLocator, String checkboxForElement) {
        scrollIntoView(checkboxLocator);
        boolean isSelected = isCheckBoxChecked(checkboxLocator);
        if (!isSelected) {
            driver.findElement(checkboxLocator).click();
            if (isCheckBoxChecked(checkboxLocator)) {
                logger.info("Checkbox for {} selected.", checkboxForElement);
                return true;
            } else {
                logger.info("Checkbox for {} could not be selected.", checkboxForElement);
            }
        } else {
            logger.info("Checkbox for {} already selected.", checkboxForElement);

        }
        return false;
    }


    public boolean uncheckCheckbox(WebElement checkbox, String checkboxForElement) {
        scrollIntoView(checkbox);
        boolean isSelected = isCheckBoxChecked(checkbox);
        if (isSelected) {
            checkbox.click();
            if (!isCheckBoxChecked(checkbox)) {
                logger.info("Checkbox for {} unselected.", checkboxForElement);
                return true;
            } else {
                logger.warn("Checkbox for {} could not be unselected.", checkboxForElement);
            }
        } else {
            logger.info("Checkbox for {} is already unselected.", checkboxForElement);
        }
        return false;
    }

    public boolean uncheckCheckbox(By checkboxLocator, String checkboxForElement) {

        scrollIntoView(checkboxLocator);
        boolean isSelected = isCheckBoxChecked(getElementWithCustomLocator(checkboxLocator));
        if (isSelected) {
            getElementWithCustomLocator(checkboxLocator).click();
            if (!isCheckBoxChecked(getElementWithCustomLocator(checkboxLocator))) {
                logger.info("Checkbox for {} unselected.", checkboxForElement);
                return true;
            } else {
                logger.warn("Checkbox for {} could not be unselected.", checkboxForElement);
            }

        } else {
            logger.info("Checkbox for {) is already unselected.", checkboxForElement);
        }
        return false;
    }

    public boolean clickRadioBtn(WebElement radioBtn, String radioBtnForElement) {
        scrollIntoView(radioBtn);
        boolean isSelected = radioBtn.getAttribute("class").contains("p-radiobutton-checked")
                || radioBtn.getAttribute("class").contains("p-highlight");
        if (!isSelected) {
            radioBtn.click();
            if (radioBtn.getAttribute("class").contains("p-radiobutton-checked")
                    || radioBtn.getAttribute("class").contains("p-highlight")) {
                logger.info("Radio Button for {} selected.", radioBtnForElement);
                return true;
            } else {
                logger.info("Radio Button for {) could not be selected.", radioBtnForElement);
            }
        } else {
            logger.info("Radio Button for {} already selected.", radioBtnForElement);
        }
        return false;
    }

    public boolean clickRadioBtn(By radioBtnLocator, String radioBtnForElement) {
        scrollIntoView(radioBtnLocator);
        boolean isSelected = getAttribute(radioBtnLocator, "class").contains("p-radiobutton-checked")
                || getAttribute(radioBtnLocator, "class").contains("p-highlight");
        if (!isSelected) {
            customClick(radioBtnLocator, "Radio button for" + radioBtnForElement);
            if (getAttribute(radioBtnLocator, "class").contains("p-radiobutton-checked")
                    || getAttribute(radioBtnLocator, "class").contains("p-highlight")) {

                logger.info("Radio Button for {} selected.", radioBtnForElement);
                return true;

            } else {
                logger.info("Radio Button for {} could not be selected.", radioBtnForElement);
            }
        } else {
            logger.info("Radio Button for {) already selected.", radioBtnForElement);

        }
        return false;
    }

    public boolean verifyAlertText(String alertText) {
        By locator = By.xpath(
                "//*[contains(translate(normalize-space(text()), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '"
                        + alertText.toLowerCase() + "')]");
        return checkElementDisplayed(locator, "Alert with Text" + alertText + "'");
    }

    public String getAlertMessage() {

        WebElement messageDiv = customWait.waitForElementToBeVisible(By.cssSelector("snack-bar-container message > div"), "Alert message",
                Duration.ofSeconds(60));
        return messageDiv.getText().trim();
    }


    public void addHighlighToElement(WebElement element, String elementName) {
        // Save the original background color of the element
        String originalStyle = element.getAttribute("style");
        scrollIntoView(element);
        js.executeScript("arguments[0].style.border='3px solid red'", element);
        // Optional: Add a delay to see the highlight effect (e.g., 2 seconds)
        try {
            Thread.sleep(2000); // Wait for 2 seconds to see the highlight
        } catch (Exception e) {
            logger.error("Error occurred while trying to highlight element {}. \nCause: {}", elementName,
                    e.getMessage());
        }
        removehighlightFromElement(element, originalStyle);
    }

    public void removehighlightFromElement(WebElement ele, String originalStyle) {
        js.executeScript("arguments[0].setAttribute('style', arguments[1]); ", ele, originalStyle);
    }

    public boolean checkDialogBoxWithHeaderDisplayed(String dialogBoxHeaderText) {
        return checkElementDisplayed(
                driver.findElement(By.xpath(
                        "//mat-dialog-container//h3[contains (normalize-space(text())," + dialogBoxHeaderText + "')]")),
                dialogBoxHeaderText + "Dialog Box");
    }


    public boolean enterIntoTextAreaInDialogBox(String dialogBoxHeaderText, String inputText) {

        return customSendKeys(By.xpath("//mat-dialog-container//h3[contains(normalize-space(text()),'"
                + dialogBoxHeaderText + "')]/following::textarea"), "Textbox in dialog"
                + dialogBoxHeaderText, inputText);
    }

    public List<String> getTextFromWebElementList(List<WebElement> elements) {
        List<String> textList = new ArrayList<>();
        for (WebElement element : elements) {
            String elementText = element.getText().trim();
            if (elementText != null && !textList.contains(elementText))
                textList.add(elementText);
        }
        return textList;

    }

    private void processRow(WebElement row, int rowIndex, String colId, Map<Integer, String> rowIndexMap) {

        try {
            List<WebElement> cells = row.findElements(By.xpath(".//div[@role='gridcell' and @col-ide='" + colId + "']"));

            if (!cells.isEmpty() && cells.get(0).isDisplayed()) {
                rowIndexMap.put(rowIndex, cells.get(0).getText().trim());
            } else {
                rowIndexMap.put(rowIndex, "");
            }
        } catch (StaleElementReferenceException e) {

// Skip and process in next iteration
        }
    }

    private void scrollDownIncrementally(WebElement viewport) {
        int scrollAmount = (int) (viewport.getSize().getHeight() * 1.7);
        new Actions(driver).scrollFromOrigin(WheelInput.ScrollOrigin.fromElement(viewport), 0, scrollAmount).perform();
    }

    public boolean scrollDownUntilElementFound(By elementXpath, WebElement scrollbarContainer) {

        boolean elementFound = false;
        WebElement element = null;
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));

        // Track the last scroll position to detect if we've reached the bottom
        long lastScrollTop = 0;
        long scrollIncrement = 150; // Start with a smaller scroll increment


        // Check for the element by scrolling down
        while (!elementFound) {
            try {
                // Try to find the element inside the scrollable div
                element = shortWait.until(ExpectedConditions.presenceOfElementLocated(elementXpath));
                elementFound = true; // Element is found, exit the loop
            } catch (TimeoutException e) {
                // If element not found, scroll down in increments
                js.executeScript("arguments[0].scrollTop = arguments[0].scrollTop +"
                        + scrollIncrement + "; return arguments[0].scrollTop;", scrollbarContainer); // Scroll down by defined increment

                // Allow time for content to load and render
                try {
                    Thread.sleep(500); // Wait 0.5 second before the next scroll
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                // Check if the scroll position has changed (detects if new content is being
                // loaded)
                long currentScrollTop = (long) js.executeScript("return arguments[0].scrollTop;", scrollbarContainer);
                if (currentScrollTop == lastScrollTop) {
                    // If no new content loaded, stop scrolling (you've reached the bottom)
                    break;
                }

                // Update last scroll position
                lastScrollTop = currentScrollTop;

                // Optionally, dynamically adjust scroll increment if needed (e.g., if we're
                // scrolling fast enough)

                if (scrollIncrement < 300) {
                    scrollIncrement += 50; // Gradually increase the increment to speed up
                }
            }
        }

        // After the element is found or we reach the bottom, ensure it's in view
        if (element != null) {
            js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element); // Smooth
            // scroll to element
        }
        // Return the result
        return elementFound;
    }


    public boolean selectOptionFromSelectDropdownByText(WebElement dropdown, String dropdownName,
                                                        String valueToSelect) {
        try {
            if (valueToSelect.isBlank() || valueToSelect == null) {
                throw new NullPointerException("Value to select is null.");
            }
            scrollIntoView(dropdown);
            clickDropdown(dropdown, dropdownName);
            Select selectDropdown = new Select(dropdown);
            selectDropdown.selectByVisibleText(valueToSelect.trim());
            WebElement selectedOption = selectDropdown.getFirstSelectedOption();
            String selectedText = selectedOption.getText();
            if (valueToSelect.equals(selectedText)) {
                logger.info("Selected value {) from dropdown {}.", valueToSelect, dropdownName);
                return true;
            }
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException(
                    "Could not find the specified value" + valueToSelect + " in + dropdownName + dropdown.");
        }
        return false;
    }

    public boolean sortColumn(String columnName, boolean ascending) {
        try {
            By columnLocator = By.xpath("//span[@ref='eText and normalize-space(text())='" + columnName
                    + "']//ancestor-or-self::div[@role='columnheader']");

            String colId = getAttribute(columnLocator, "col-id");
            String sortValue = getAttribute(columnLocator, "aria-sort");
            List<String> originalList = getTextFromWebElementListWithLocator(colId);
            logger.info("Original List: {}", originalList);
            if (ascending) {
                handleSort(columnLocator, ascending);
            } else {
                handleSort(columnLocator, false);
            }


            List<String> sortedList = getTextFromWebElementListWithLocator(colId);
            if (CustomStringUtils.verifySorted(originalList, sortedList, ascending)) {
                logger.info("Column {} sorted successfully.", columnName);
                return true;
            } else {
                logger.error("Column {} was not sorted.", columnName);
            }

        } catch (Exception e) {
            logger.error("An error occurred while sorting column {}. Cause {): ", columnName, e.getMessage());
        } finally {
            clearFilterValues(columnName);
        }

        return false;
    }

    private List<String> getTextFromWebElementListWithLocator(String colId) {
        List<String> textList =new ArrayList<>();
        return textList;
    }

    public boolean pressEnter(WebElement locator, String elementName) {
        locator.sendKeys(Keys.ENTER);
        logger.info("Pressed Enter key in {}.", elementName);
        return true;
    }
}





