package managers;

import io.github.bonigarcia.wdm.WebDriverManager;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.BrowserOptionsUtility;
import utilities.ConfigReader;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public class DriverManager {

    private static final Logger logger = LoggerFactory.getLogger(DriverManager.class);
    String browser = ConfigReader.getConfigReader().getProperty("browser", "chrome");
    String execution = ConfigReader.getConfigReader().getProperty("execution", "local");
    String headless = ConfigReader.getConfigReader().getProperty("headless", "true");
    private WebDriver driver;

    private static void applyWindowSize(WebDriver driver, String headless) {
        if (headless.equalsIgnoreCase("true")) {
            driver.manage().window().setSize(new Dimension(1920, 5000));
        } else {
            driver.manage().window().maximize();
        }

    }

    public WebDriver initializeDriver() throws IOException {
        try {
            logger.info("Initializing WebDriver with browser: {}, execution: {}, headless: {}", browser, execution, headless);
            
            // Validate configuration
            if (browser == null || browser.trim().isEmpty()) {
                throw new IllegalArgumentException("Browser property not configured. Please check config.properties");
            }
            if (execution == null || execution.trim().isEmpty()) {
                throw new IllegalArgumentException("Execution property not configured. Please check config.properties");
            }

            if (execution.equalsIgnoreCase("grid")) {
                logger.info("Initializing Grid Driver...");
                initializeGridDriver();
            } else {
                logger.info("Initializing Local Driver...");
                initializeLocalDriver();
            }
            
            // Validate driver was successfully created
            if (driver == null) {
                throw new RuntimeException("Driver initialization failed: WebDriver instance is null. " +
                        "Browser session was not created. Check browser installation and driver version compatibility.");
            }
            
            logger.info("WebDriver instance created successfully. Configuring timeouts and preferences...");
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(70));
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            driver.manage().deleteAllCookies();
            applyWindowSize(driver, headless);
            logger.info("WebDriver successfully initialized and ready for use");

        } catch (Exception e) {
            String errorMsg = "WebDriver initialization failed\n" +
                            "Browser: " + browser + "\n" +
                            "Execution Mode: " + execution + "\n" +
                            "Headless: " + headless + "\n" +
                            "Root Cause: " + e.getClass().getSimpleName() + " - " + e.getMessage();
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
        return driver;
    }

    private void initializeGridDriver() throws MalformedURLException {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        switch (browser.toLowerCase()) {
            case "chrome":
                capabilities.setBrowserName("chrome");
                ChromeOptions chromeOptions = BrowserOptionsUtility.getChromeOptions(headless);
                capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
                break;
            case "firefox":
                capabilities.setBrowserName("firefox");
                BrowserMobProxy proxy = new BrowserMobProxyServer();
                proxy.start(0);
                Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);
                FirefoxOptions firefoxoptions = BrowserOptionsUtility.getFirefoxOptions(headless, seleniumProxy);
                capabilities.setCapability(FirefoxOptions.FIREFOX_OPTIONS, firefoxoptions);
                break;
            case "edge":
                capabilities.setBrowserName("edge");
                EdgeOptions edgeOptions = BrowserOptionsUtility.getEdgeOptions(headless);
                capabilities.setCapability(EdgeOptions.CAPABILITY, edgeOptions);
                break;
            default:
                logger.error("Unsupported browser for Grid: " + browser);
                throw new IllegalArgumentException("Unsupported browser for Grid: " + browser);
        }
        driver = new RemoteWebDriver(new URL("http://192.168.0.117:4444"), capabilities);

    }

    private void initializeLocalDriver() {
        switch (browser.toLowerCase()) {
            case "chrome":
                try {
                    logger.info("Setting up ChromeDriver using WebDriverManager...");
                    logger.info("WebDriverManager will automatically:");
                    logger.info("  1. Detect your Chrome version");
                    logger.info("  2. Download matching ChromeDriver");
                    logger.info("  3. Cache it locally for future runs");
                    
                    // Force WebDriverManager to check for new version
                    WebDriverManager.chromedriver()
                            .clearDriverCache()  // Clear old cache if any
                            .setup();            // Download and setup
                    
                    logger.info("✅ WebDriverManager setup completed successfully");
                    logger.info("ChromeDriver has been downloaded and configured");
                    
                    ChromeOptions chromeOptions = BrowserOptionsUtility.getChromeOptions(headless);
                    logger.info("Chrome options configured. Creating ChromeDriver instance...");
                    logger.info("Attempting to launch Chrome browser...");
                    
                    driver = new ChromeDriver(chromeOptions);
                    
                    if (driver == null) {
                        throw new RuntimeException("ChromeDriver instance creation returned null");
                    }
                    logger.info("✅ ChromeDriver successfully initialized and session created");
                    logger.info("Browser is now ready for testing");
                } catch (Exception e) {
                    logger.error("\n" +
                            "❌ FAILED to initialize ChromeDriver\n" +
                            "Error: {}\n" +
                            "\n" +
                            "SOLUTIONS:\n" +
                            "1. Verify Chrome browser IS INSTALLED\n" +
                            "   - Download from: https://www.google.com/chrome/\n" +
                            "   - Default location: C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe\n" +
                            "\n" +
                            "2. Check Chrome version matches driver\n" +
                            "   - Clear WebDriverManager cache: Delete C:\\Users\\{username}\\.wdm\\\n" +
                            "   - Run tests again (it will download correct driver)\n" +
                            "\n" +
                            "3. Ensure no Chrome process is locked\n" +
                            "   - Run: taskkill /IM chrome.exe /F\n" +
                            "   - Run: taskkill /IM chromedriver.exe /F\n" +
                            "\n" +
                            "4. Try headless mode (may help with version issues)\n" +
                            "   - In config.properties: headless=true\n" +
                            "\n" +
                            "Root cause details:",
                            e.getMessage(), e);
                    throw e;
                }
                break;
            case "firefox":
                try {
                    logger.info("Setting up FirefoxDriver using WebDriverManager...");
                    WebDriverManager.firefoxdriver()
                            .clearDriverCache()
                            .setup();
                    logger.info("✅ WebDriverManager setup completed successfully");
                    
                    BrowserMobProxy proxy = new BrowserMobProxyServer();
                    proxy.start(0);
                    Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);
                    FirefoxOptions firefoxOptions = BrowserOptionsUtility.getFirefoxOptions(headless, seleniumProxy);
                    logger.info("Firefox options configured. Creating FirefoxDriver instance...");
                    driver = new FirefoxDriver(firefoxOptions);
                    
                    if (driver == null) {
                        throw new RuntimeException("FirefoxDriver instance creation returned null");
                    }
                    logger.info("✅ FirefoxDriver successfully initialized and session created");
                } catch (Exception e) {
                    logger.error("❌ FAILED to initialize FirefoxDriver: {}\n" +
                            "Ensure Firefox is installed from: https://www.mozilla.org/firefox/\n" +
                            "Default location: C:\\Program Files\\Mozilla Firefox\\firefox.exe", 
                            e.getMessage(), e);
                    throw e;
                }
                break;
            case "edge":
                try {
                    logger.info("Setting up EdgeDriver using WebDriverManager...");
                    WebDriverManager.edgedriver()
                            .clearDriverCache()
                            .setup();
                    logger.info("✅ WebDriverManager setup completed successfully");
                    
                    EdgeOptions edgeOptions = BrowserOptionsUtility.getEdgeOptions(headless);
                    logger.info("Edge options configured. Creating EdgeDriver instance...");
                    driver = new EdgeDriver(edgeOptions);
                    
                    if (driver == null) {
                        throw new RuntimeException("EdgeDriver instance creation returned null");
                    }
                    logger.info("✅ EdgeDriver successfully initialized and session created");
                } catch (Exception e) {
                    logger.error("❌ FAILED to initialize EdgeDriver: {}\n" +
                            "Ensure Edge is installed from: https://www.microsoft.com/edge\n" +
                            "Default location: C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe",
                            e.getMessage(), e);
                    throw e;
                }
                break;
            default:
                logger.error("Unsupported browser: " + browser);
                throw new IllegalArgumentException("Unsupported browser: " + browser);
        }
    }
}


