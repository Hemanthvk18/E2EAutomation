package utilities;

import managers.TestContextManager;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class CustomWait {
    private static final Logger logger = LoggerFactory.getLogger(CustomWait.class);
    private final WebDriver driver;
    private final TestContextManager context;
    private Duration defaultPolling = Duration.ofMillis(500);

    public CustomWait(WebDriver driver, TestContextManager context) {
        this.driver = driver;
        this.context = context;
    }

    private WebDriverWait createWait() {
        return createWait(context.getCurrentTimeout());
    }

    private WebDriverWait createWait(Duration timeout) {
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        wait.pollingEvery(defaultPolling);
        wait.ignoring(StaleElementReferenceException.class, NoSuchElementException.class);
        return wait;
    }

    //== WebElement-returning core method ===
    public WebElement waitForElement(ExpectedCondition<WebElement> condition, String elementName, String conditionType) {
        return waitForElement(condition, elementName, conditionType, context.getCurrentTimeout());
    }

    public WebElement waitForElement(ExpectedCondition<WebElement> condition, String elementName, String conditionType, Duration timeout) {
        try {
            // logger.info("() is ().", elementName, conditionType);
            return createWait(timeout).until(condition);
        } catch (TimeoutException e) {
            logger.warn("{} not {} after {} seconds", elementName, conditionType, timeout.getSeconds());
        } catch (Exception e) {
            logger.error("Unexpected error waiting for {} to be {}: {}", elementName, conditionType, e.getMessage());
        }
        return null;
    }


    // ===Element-based methods ===
    public WebElement waitForElementToBeClickable(WebElement element, String elementName) {
        return waitForElement(ExpectedConditions.elementToBeClickable(element), elementName, "clickable");
    }

    public WebElement waitForElementToBeClickable(WebElement element, String elementName, Duration timeout) {
        return waitForElement(ExpectedConditions.elementToBeClickable(element), elementName, "clickable", timeout);

    }

    public WebElement waitForElementToBeVisible(WebElement element, String elementName) {
        return waitForElement(ExpectedConditions.visibilityOf(element), elementName, "visible");
    }


    public WebElement waitForElementToBeVisible(WebElement element, String elementName, Duration timeout) {
        return waitForElement(ExpectedConditions.visibilityOf(element), elementName, "visible", timeout);
    }

    public boolean waitForElementToBeInvisible(WebElement element, String elementName) {
        try {
            return createWait().until(ExpectedConditions.invisibilityOf(element));
        } catch (TimeoutException e) {
            logger.warn("{} is still visible after timeout.", elementName);
        }
        return false;
    }


    public boolean waitForElementToBeInvisible(WebElement element, String elementName, Duration duration) {
        try {
            return createWait(duration).until(ExpectedConditions.invisibilityOf(element));
        } catch (TimeoutException e) {
            logger.warn("{} is still visible after timeout.", elementName);
        }
        return false;
    }

//=== Locator-based methods ===

    public WebElement waitForElementToBeVisible(By locator, String elementName) {
        return waitForElement(ExpectedConditions.visibilityOfElementLocated(locator), elementName, "visible");
    }

    public WebElement waitForPresenceOfNestedElement(WebElement mainLocator, By childLocator, String elementName) {
        return waitForElement(ExpectedConditions.presenceOfNestedElementLocatedBy(mainLocator, childLocator), elementName, "visible");
    }

    public WebElement waitForElementToBeVisible(By locator, String elementName, Duration timeout) {
        return waitForElement(ExpectedConditions.visibilityOfElementLocated(locator), elementName, "visible", timeout);
    }

    public WebElement waitForElementToBeClickable(By locator, String elementName) {
        return waitForElement(ExpectedConditions.elementToBeClickable(locator), elementName, "clickable");
    }

    public WebElement waitForElementToBeClickable(By locator, String elementName, Duration timeout) {
        return waitForElement(ExpectedConditions.elementToBeClickable(locator), elementName, "clickable", timeout);
    }

    public WebElement waitForElementPresent(By locator, String elementName) {
        return waitForElement(ExpectedConditions.presenceOfElementLocated(locator), elementName, "present");
    }

    public WebElement waitForElementToBePresent(By locator, String elementName, Duration timeout) {
        return waitForElement(ExpectedConditions.presenceOfElementLocated(locator), elementName, "present", timeout);
    }

    public boolean waitForElementToBeInvisible(By locator, String elementName) {
        return waitForElementToBeInvisible(locator, elementName, context.getCurrentTimeout().getSeconds());
    }

    public boolean waitForElementToBeInvisible(By locator, String elementName, long timeoutSeconds) {
        try {
            return createWait(Duration.ofSeconds(timeoutSeconds))
                    .until(ExpectedConditions.invisibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            logger.warn("{} is still visible after {} seconds.", elementName, timeoutSeconds);
        }
        return false;
    }

    // Text-based methods=
    public boolean waitForElementText(WebElement element, String elementName, String textToBePresent) {
        if (waitForElementToBeVisible(element, elementName) != null) {
            return createWait().until(ExpectedConditions.textToBePresentInElement(element, textToBePresent));
        }
        return false;
    }

    public boolean waitForElementText(By locator, String elementName, String textToBePresent) {
        if (waitForElementToBeVisible(locator, elementName) != null) {
            return createWait().until(ExpectedConditions.textToBePresentInElementLocated(locator, textToBePresent));
        }
        return false;
    }

    //=== Multi-element methods ===
    public List<WebElement> waitForAllElementsToBeVisible(List<WebElement> elements) {
        try {
            return createWait().until(ExpectedConditions.visibilityOfAllElements(elements));
        } catch (TimeoutException e) {
            logger.warn("Timed out waiting for elements to be visible");
        }
        return null;
    }

    public List<WebElement> waitForAllElementsToBeVisible(By locator) {
        try {
            return createWait().until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
        } catch (TimeoutException e) {
            logger.warn("Timed out waiting for elements to be visible");
        }
        return null;
    }

    public List<WebElement> waitForAllElementsToBeVisible(By locator, Duration timeout) {
        try {
            return createWait(timeout).until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
        } catch (TimeoutException e) {
            logger.warn("Timed out waiting for elements to be visible");
        }
        return null;
    }

    public List<WebElement> waitForAllElementsToBePresent(By locator) {
        try {
            return createWait().until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
        } catch (TimeoutException e) {
            logger.warn("Timed out waiting for elements to be present");
        }
        return null;
    }

    public WebElement waitForOptionByAriaLabel(By optionsBy, String value) {
        return waitForOptionByAriaLabel(optionsBy, value, context.getCurrentTimeout());
    }

    public WebElement waitForOptionByAriaLabel(By optionsBy, String value, Duration timeout) {
        final String needle = (value == null) ? "" : value.trim();
        if (needle.isEmpty()) return null;
        try {
            return createWait(timeout).until(driver -> {
                List<WebElement> opts = driver.findElements(optionsBy);
                for (WebElement opt : opts) {
                    try {
                        String label = opt.getAttribute("aria-label");
                        if (label != null && label.trim().equalsIgnoreCase(needle) && opt.isDisplayed()) {
                            return opt;
                        }
                    } catch (StaleElementReferenceException ignored) { /* re-render, keep polling */ }
                }
                return null; // continue waiting
            });
        } catch (TimeoutException te) {
            logger.warn("Option with aria-label '{}' not found within timeout using: {}", needle, optionsBy);
            return null;
        }
    }


    //=== Custom condition=

    public void waitForCondition(ExpectedCondition<Boolean> condition, String description) {
        waitForCondition(condition, context.getCurrentTimeout(), description);
    }

    public void waitForCondition(ExpectedCondition<Boolean> condition, Duration timeout, String description) {
        try {
            createWait(timeout).withMessage("Timeout waiting for: "+ description).until(condition);
        } catch (TimeoutException e) {
            logger.error("Condition not met: {}", description);
        }
    }

    //=== Static wait ===
    public void waitFor(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public <T> T until(Function<WebDriver, T> condition) {
        return createWait().until(driver -> {
            try {
                return condition.apply(driver);
            } catch (StaleElementReferenceException ignored) {
                return null; // let WebDriverWait poll again
            }
        });
    }

    public void waitForGridReady(By gridRoot) {
        until(d -> {
            WebElement root = d.findElement(gridRoot);
            boolean headerOk = !root.findElements(By.cssSelector(".ag-header")).isEmpty();
            boolean overlayVisible = root.findElements(By.cssSelector(".ag-overlay")).stream()
                    .anyMatch(WebElement::isDisplayed);
            return headerOk && !overlayVisible;
        });
    }

    //=== Config ===
    public void setDefaultPolling(Duration pollingDuration) {
        this.defaultPolling = pollingDuration;
    }
}






