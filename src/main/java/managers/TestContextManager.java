package managers;

import org.apache.xmlbeans.UserType;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import utilities.*;
import data.ScenarioDataProvider;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestContextManager {

    public static final By GRID_ROOT = By.cssSelector(".ag-root");
    public Map<String, List<Map<String, String>>> columnNameColIdMapAllSheets;
    public List<Map<String, String>> scenarioDataFromASheet;
    private PageObjectManager pageObjectManager;
    private DriverManager webDriverManager;
    private CustomWebElementActions customActions;
    private CustomWait customWait;
    private Duration currentTimeout;
    private WebDriverWait webdriverWait;
    private WebDriver driver;
    private String scenarioName;
    private NetworkCaptureUtil networkCaptureUtil;
    private Map<String, String> scenarioFirstRowData;
    private String userKey;
    private UserType userType;
    private String userRole;
    private String category;
    private Map<String, Object> data = new HashMap<>();
    private ScenarioDataProvider provider;
    private String currentModule;

    public TestContextManager() {
        this.currentTimeout = Duration.ofSeconds(60);
    }

    public ScenarioDataProvider data() {
        if (provider == null) provider = new ScenarioDataProvider(this);
        return provider;
    }

    public void setScenarioData(List<Map<String, String>> scenarioDataFromASheet) {
        this.scenarioDataFromASheet = scenarioDataFromASheet;
    }

    public List<Map<String, String>> getScenarioData() {
        return scenarioDataFromASheet;
    }


    public void setColumnNameColIdMapAllSheets(Map<String, List<Map<String, String>>> columnNameColIdMapAllSheets) {
        this.columnNameColIdMapAllSheets = columnNameColIdMapAllSheets;
    }

    public Map<String, List<Map<String, String>>> getColumnNameColIdMapAllSheets() {
        return columnNameColIdMapAllSheets;
    }

    public void setScenarioFirstRowData(Map<String, String> scenarioFirstRowData) {
        this.scenarioFirstRowData = scenarioFirstRowData;
    }

    public Map<String, String> getScenarioFirstRowData() {
        return scenarioFirstRowData;
    }

    public void setWebdriverWait(WebDriverWait webdriverWait) {
        this.webdriverWait = webdriverWait;
    }

    public WebDriverWait getWebdriverWait() {
        return webdriverWait;
    }

    public void setWebDriverManager(DriverManager webDriverManager) {
        this.webDriverManager = webDriverManager;
    }

    public DriverManager getWebDriverManager() {
        return webDriverManager;
    }

    public Object get(String key) {
        return data.get(key);
    }

    public void put(String key, Object value) {
        data.put(key, value);
    }
    public void remove(String key) {
        data.remove(key);
    }

    public boolean contains(String key) {
        return data.containsKey(key);
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserCategory(String category) {
        this.category = category;
    }

    public String getUserCategory() {
        return category;
    }

    public void resetData() {
        if (provider != null) provider.reset();
    }

    public String getCurrentModule() {
        return currentModule;
    }

    public void setCurrentModule(String currentModule) {
        this.currentModule = currentModule;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public Duration getCurrentTimeout() {
        return currentTimeout;
    }

    public void setCurrentTimeout(Duration currentTimeout) {
        this.currentTimeout = currentTimeout;
    }

    public String getInputDataExcelPath(String moduleName) {
        String env = ConfigReader.getConfigReader().getProperty("env");
        String excelPath = "";
        switch (env.toLowerCase()) {
            case "dev":
                excelPath = FileConstants.INPUT_DATA_DEV + moduleName + "_Data.xlsx";
                break;
            case "int":
                excelPath = FileConstants.INPUT_DATA_DEV + moduleName + "_Data.xlsx";
                break;
            case "stg":
                excelPath = FileConstants.INPUT_DATA_STG + moduleName + "_Data.xlsx";
                break;
        }

        return excelPath;

    }

    public NetworkCaptureUtil getNetworkCaptureUtil() {
        return networkCaptureUtil;
    }

    public void setNetworkCaptureUtil(NetworkCaptureUtil networkCaptureUtil) {
        this.networkCaptureUtil = networkCaptureUtil;
    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    public WebDriver getDriver() {
        return driver;
    }

    public void setPageObjectManager(PageObjectManager pageObjectManager) {
        this.pageObjectManager = pageObjectManager;
    }

    public PageObjectManager getPageObjectManager() {
        return pageObjectManager;
    }

    public void setCustomActions(CustomWebElementActions customActions) {
        this.customActions = customActions;
    }

    public CustomWebElementActions getCustomActions() {
        return customActions;
    }

    public void setCustomWait(CustomWait customWait) {
        this.customWait = customWait;
    }

    public CustomWait getCustomWait() {
        return customWait;
    }




}


