package utilities;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class BrowserOptionsUtility {
    private static final Logger logger = LoggerFactory.getLogger(BrowserOptionsUtility.class);
    private static final String[] COMMON_OPTIONS = {"--disable-blink-features=AutomationControlled",
            "--force-device-scale-factor=1", "--disable-notifications", "--remote-allow-origins=*"};
    static String downloadDir = FileConstants.DOWNLOAD_DIRECTORY;


    public static ChromeOptions getChromeOptions(String headless) {
        ChromeOptions options = new ChromeOptions();

        for (String arg : COMMON_OPTIONS) {
            options.addArguments(arg);
        }

        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        // ✅ SAFE HEADLESS CHECK
        if (headless != null && headless.equalsIgnoreCase("true")) {
            logger.info("Running in headless mode.");
            options.addArguments("--headless=new");
        } else {
            logger.info("Running in normal mode.");
        }

        return options;
    }

//    public static ChromeOptions getChromeOptions(String headless) {
//        ChromeOptions options = new ChromeOptions();
//        for (String arg : COMMON_OPTIONS) {
//            options.addArguments(arg);
//        }
//
//        options.addArguments("--disable-features=NetworkService");
//        options.addArguments("--disable-popup-blocking");
//        options.addArguments("--log-level=3");
//        options.addArguments("--disable-gpu");
//        options.addArguments("--no-sandbox");
//        options.addArguments("--disable-dev-shm-usage");
//        options.setAcceptInsecureCerts(true);
//        options.addArguments("--ignore-certificate-errors");
//        options.addArguments("--allow-insecure-localhost");
//// Set Chrome Preferences
//
//        HashMap<String, Object> prefs = new HashMap<>();
//        prefs.put("download.default_directory", downloadDir); // absolute path
//        prefs.put("download.prompt_for_download", false);
//        prefs.put("download.directory_upgrade", true);
//        prefs.put("plugins.always_open_pdf_externally", true); // <- change to true
//        prefs.put("safebrowsing.enabled", true);
//
//// Improve automatic downloads
//        prefs.put("profile.default_content_setting_values.automatic downloads", 1);
//        prefs.put("profile.content_settings.exceptions.automatic downloads.*.setting", 1);
//
//// Don't open after download (Chrome honors this; doesn't stop Save As dialogs though)
//        prefs.put("download.open_after_download", false);
//
//// Optional (internal/lab env only): relax download protection
//// prefs.put("safebrowsing.disable_download_protection", true);
//        options.setExperimentalOption("prefs", prefs);
//
//// Optional: stabilize download UI behavior on newer Chrome
//        options.addArguments("--disable-features=DownloadBubble");
//        options.setExperimentalOption("prefs", prefs);
//
//
//// Headless mode configuration
//
//        if ("true".equalsIgnoreCase(headless.trim())) {
//            logger.info("Running in headless mode.");
//            options.addArguments("--headless=new");
//
//        } else {
//            logger.info("Headless mode is set to False. Running with Broswer GUI");
//        }
//        return options;
//    }


    public static FirefoxOptions getFirefoxOptions(String headless, Proxy seleniumProxy) {
        FirefoxOptions options = new FirefoxOptions();
        // Setting Binary since the exe is present in this folder and it is not added to
        // system variables. Path needs to be updated for each user.
        options.setBinary("C:\\Users\\xxxx\\AppData\\Local\\Mozilla Firefox\\firefox.exe");
        options.addPreference("dom.notifications.enabled", false);
        options.setCapability("moz:webdriverClick", true);
        options.addPreference("layout.css.devPixelsPerPx", "1.0"); // For standard 1x scaling
        options.addPreference("browser.zoom.siteSpecific", false); // Set to false to disable site-specific zoo
        options.setCapability("moz:firefoxOptions", "{loggingPrefs: (browser: 'SEVERE'}}");
        if ("true".equalsIgnoreCase(headless)) {
            logger.info("Running in headless mode.");
            options.addArguments("-headless");
        }


        FirefoxProfile profile = new FirefoxProfile();
        profile.setPreference("browser.download.dir", downloadDir);
        profile.setPreference("browser.download.folderList", 2); // 2 means use specified directory
        profile.setPreference("browser.download.useDownloadDir", true); // Automatically download
        profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/octet-stream");
        options.setProfile(profile);

// Set up BrowserMob Proxy first
        options.setProxy(seleniumProxy);
        return options;

    }

    public static EdgeOptions getEdgeOptions(String headless) {
        EdgeOptions options = new EdgeOptions();
        for (String arg : COMMON_OPTIONS) {
            options.addArguments(arg);
        }
        options.setExperimentalOption("prefs",
                Map.of("download.default_directory", downloadDir, "download.prompt_for_download", false, "download.directory_upgrade", true, "plugins.always_open_pdf_externally", true))
        ;
        options.addArguments("--log-level=3");
        options.setCapability("goog:loggingPrefs", "(browser: 'SEVERE'}");
        if ("true".equalsIgnoreCase(headless)) {
            logger.info("Running in headless mode.");
            options.addArguments("--headless");
        }
        return options;
    }
}


