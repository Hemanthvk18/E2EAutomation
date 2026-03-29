package stepdefinitions;

import io.cucumber.java.*;
import io.qameta.allure.Allure;
import managers.DriverManager;
import managers.PageObjectManager;
import managers.TestContextManager;
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.LoginPage;
import utilities.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Hook {
    private static final Logger logger = LoggerFactory.getLogger(Hook.class);
    static String allureReportDirectory;
    private static UserPoolManager userPoolManager;
    private static UserCapabilityMapper capabilityMapper;
    String url = ConfigReader.getConfigReader().getUrl();
    WebDriver driver;
    private TestContextManager context;
    private DriverManager driverManager;
    private PageObjectManager pageObjectManager;

    public Hook(TestContextManager context, DriverManager driverManager, PageObjectManager pageObjectManager)
            throws Exception {
        this.context = context;
        this.driverManager = driverManager;
        this.pageObjectManager = pageObjectManager;

    }


    @BeforeAll
    public static void before_all() throws Exception {
        // Initialize user pool from config.properties
        userPoolManager = new UserPoolManager(ConfigReader.getConfigReader());
        // Initialize capability mapper (maps tags to categories)
        capabilityMapper = new UserCapabilityMapper(ConfigReader.getConfigReader());
        String timestamp = new SimpleDateFormat("MMM-dd-yyyy-HH_mm").format(new Date());
        String baseDir = "allure-reports";
        allureReportDirectory = baseDir + "/allure-report-" + timestamp;
        java.nio.file.Files.createDirectories(java.nio.file.Paths.get(baseDir));
        java.nio.file.Files.createDirectories(java.nio.file.Paths.get("logs"));
    }


    @AfterAll()
    public static void after_all() {
        try {
            FileUtility.copyDir(
                    Paths.get(".last-history", "history"),
                    Paths.get("allure-results", "history")
            );
            String monthAndDay = new SimpleDateFormat("MMM-dd").format(new Date());
            String filename = "eTool Automation Test Report" + monthAndDay + ".html";
            String reportTitle = "eTool Cucumber Automation Report";
            AllureUtility.generateAllureReport(allureReportDirectory, "allure-results", reportTitle, filename,
                    ConfigReader.getConfigReader().getProperty("browser"),
                    ConfigReader.getConfigReader().getProperty("url"));

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Before(order = 1)
    public void beforeScenario(Scenario scenario) throws Exception {


        // Step 1: Determine test needs from @tags
        Set<String> capabilities = determineCapabilities(scenario);
        // Example: @admin tag → capabilities = {admin}

        // Step 2: Find suitable user category
        String category = findSuitableCategory(capabilities);
        // Example: {admin} → category = "test_admin"

        // Step 3: Get available user from pool (blocks other tests)
        String userKey = userPoolManager.acquireUser(category);
        context.setUserKey(userKey);
        context.setUserCategory(category);
        // Example: "TEST_ADMIN1" (no other test uses it now)

        // Step 4: Extract module name from @module: tag
        String moduleFromTag = scenario.getSourceTagNames().stream()
                .filter(t -> t.startsWith("@module:"))
                .map(t -> t.substring("@module:".length()))
                .findFirst()
                .orElse(null);
        // Example: @module:HomePage → module = "HomePage"
        context.setCurrentModule(moduleFromTag);
        ThreadContext.put("scenarioName", scenario.getName());
        context.setScenarioName(scenario.getName());
        logger.info("-----------------------------------------------------------");
        logger.info("Starting Scenario: {}", scenario.getName());
        logger.info("Module for this scenario: {}", moduleFromTag);
        logger.info("-----------------------------------------------------------");
        logger.info(" Initializing WebDriver for scenario: {}", scenario.getName());
        logger.info("Acquired {} user: {} for scenario: {}", category, userKey, scenario.getName());

        // Step 5: Initialize WebDriver (browser)
        try {
            logger.info("About to initialize WebDriver for scenario: {}", scenario.getName());
            driver = driverManager.initializeDriver();
            logger.info("WebDriver initialized successfully");
        } catch (Exception ex) {
            logger.error("CRITICAL: WebDriver initialization failed for scenario: {}. Error: {}",
                    scenario.getName(), ex.getMessage(), ex);
            throw ex;
        }
        // Step 6: Setup context with all tools
        context.setDriver(driver);
        context.setPageObjectManager(pageObjectManager);
        context.setWebdriverWait(new WebDriverWait(driver, context.getCurrentTimeout()));
        context.setCustomWait(new CustomWait(driver, context));
        context.setCustomActions(
                new CustomWebElementActions(driver, context.getCustomWait(), context.getWebdriverWait()));
        context.setNetworkCaptureUtil(new NetworkCaptureUtil(driver));
        context.getNetworkCaptureUtil().startCapturing();
        // Step 7: Navigate to URL
        driver.get(url);
        context.getCustomActions().waitForPageLoad();
        logger.info("Loading URL for Login page:{} for scenario: {}", url, scenario.getName());
        context.getPageObjectManager().getLoginPage().loadLoginPageWithRetry(url);
        // Step 9: Perform login with env variables
        performLogin(userKey, category);
    }

    private Set<String> determineCapabilities(Scenario scenario) {
        Set<String> capabilities = new HashSet<>();
        for (String tag : scenario.getSourceTagNames()) {
            if (tag.startsWith("@")) {
                String cleanTag = tag.substring(1).toLowerCase();
                if (capabilityMapper.getCategoriesForCapability(cleanTag) != null) {
                    capabilities.add(cleanTag);
                }
            }
        }

        if (capabilities.isEmpty()) {
            capabilities.add("test_user");
        }
        return capabilities;
    }


    private String findSuitableCategory(Set<String> capabilities) {
        // Handle special capabilities first
        if (capabilities.contains("normal_admin_only")) {
            return "normal_admin";
        }

        // Check for admin capability
        if (capabilities.contains("admin")) {
            String defaultCategory = capabilityMapper.getDefaultCategoryForCapability("admin");
            if (!defaultCategory.isEmpty()) {
                return defaultCategory;
            }
            return "test admin"; // Prefer test admin if no default

        }

        // Default to test user
        return "test_normal";
    }

    private void performLogin(String userKey, String category) {
        ConfigReader config = ConfigReader.getConfigReader();
        String email = config.getEmail("EMAIL");
        String password = config.getPass("PASSWORD");

//        validateCredentials(userKey, email, password);
        AllureUtility.addSubStepForData("User ID Data", "User ID", email);

        LoginPage loginPage = context.getPageObjectManager().getLoginPage();
        loginPage.login(email, password);
    }


    private void validateCredentials(String userKey, String email, String password) {
        StringBuilder sb = new StringBuilder();
        if (email == null || email.isBlank()) sb.append("email, ");
        if (password == null || password.isBlank()) sb.append("password, ");
        if (sb.length() > 0) {
            String missing = sb.substring(0, sb.length() - 2);
            throw new IllegalArgumentException("Missing credentials for userKey'" + userKey + ":" + missing);
        }
    }


    @After(order = 2)
    public void afterScenario(Scenario scenario) throws Exception {
        // Capture screenshot if failed
        try {
            if (scenario.isFailed()) {
                logger.error("Scenario failed: {}", scenario.getName());
            } else if (scenario.getStatus().toString().toUpperCase().contains("SKIPPED")) {
                logger.info("Scenario skipped: {}", scenario.getName());
            } else {
                logger.info("Scenario passed: {}", scenario.getName());
            }
        } catch (Exception e) {
            logger.error("Failed to capture screenshot for scenario: {}", scenario.getName(), e.getMessage());
        } finally {
            context.resetData();
            Allure.addAttachment(context.getCustomActions().getUrl(), new ByteArrayInputStream(
                    ((TakesScreenshot) context.getDriver()).getScreenshotAs(OutputType.BYTES)));

            if (context.getUserKey() != null && context.getUserCategory() != null) {
                // Release user back to pool for other tests
                userPoolManager.releaseUser(context.getUserCategory(), context.getUserKey());
                logger.info("Released () user: {}", context.getUserCategory(), context.getUserKey());

                // Close browser
                if (context.getDriver() != null) {
                    logger.info("Closing WebDriver after scenario execution.");
                    context.getDriver().quit();
                }

                ThreadContext.clearAll();
                // Stop network capture
                context.getNetworkCaptureUtil().stop();
                // Cleanup temp files
                FileUtility.cleanUpFolder(FileConstants.DOWNLOAD_DIRECTORY);


            }
        }
    }
}
