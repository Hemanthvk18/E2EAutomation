package utilities;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v143.network.Network;
import org.openqa.selenium.devtools.v143.network.Network.GetResponseBodyResponse;
import org.openqa.selenium.devtools.v143.network.model.RequestId;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class NetworkCaptureUtil {
    private static final Logger logger = LoggerFactory.getLogger(NetworkCaptureUtil.class);
    private WebDriver driver;
    private DevTools devTools;
    private BrowserMobProxy proxy;
    private LinkedHashMap<String, String> apiResponses = new LinkedHashMap<>();
    private String browserType;

    public NetworkCaptureUtil(WebDriver driver) {
        this.driver = driver;
        detectBrowserType();
        setupNetworkCapture();
    }


    private void detectBrowserType() {
        if (driver instanceof ChromeDriver) {
            browserType = "chrome";
        } else if (driver instanceof EdgeDriver) {
            browserType = "edge";
        } else if (driver instanceof FirefoxDriver) {
            browserType = "firefox";
        } else {
            throw new IllegalStateException("Unsupported browser type for network capture : " + driver);
        }
    }

    private void setupNetworkCapture() {
        switch (browserType) {
            case "chrome":
            case "edge":
                setupDevTools();
                break;
            case "firefox":
                setupProxy();
                break;
        }
    }

    private void setupDevTools() {
        try {
            if (driver instanceof ChromeDriver) {
                devTools = ((ChromeDriver) driver).getDevTools();
            } else if (driver instanceof EdgeDriver) {
                devTools = ((EdgeDriver) driver).getDevTools();
            }
            
            devTools.createSession();
            devTools.send(Network.enable(
                    Optional.empty(), // maxTotalBufferSize
                    Optional.empty(), // maxResourceBufferSize
                    Optional.empty(), // maxPostDataSize
                    Optional.of(false),// includeRawHeaders (or true if needed),
                    Optional.of(false)
            ));

            devTools.addListener(Network.responseReceived(), response -> {
                RequestId requestId = response.getRequestId();
                String url = response.getResponse().getUrl();
                try {
                    GetResponseBodyResponse responseBody = devTools.send(Network.getResponseBody(requestId));
                    if (responseBody != null && responseBody.getBody() != null) {
                        apiResponses.put(url, responseBody.getBody());
                    }
                } catch (Exception e) {
                    // Silently ignore response capture errors
                }
            });
            logger.info("✅ DevTools network capture initialized successfully for {}", browserType);
            
        } catch (NoSuchMethodError | Exception e) {
            logger.warn("⚠️ Failed to initialize DevTools for network capture ({}). " +
                    "Tests will continue without network capture. This is not a test failure.", 
                    e.getClass().getSimpleName());
            devTools = null;
        }
    }

    private void setupProxy() {
        proxy = new BrowserMobProxyServer();
        proxy.start(0);
        Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);
        //System.out.println("Using BrowserMob Proxy for Firefox");
        proxy.newHar("networkCapture");
    }

    public void startCapturing() {
        if ("firefox".equals(browserType)) {
            proxy.newHar("networkCapture");
        }
    }

    public List<String> getAllApiResponses(String partialUrl) {
        List<String> matches = new ArrayList<>();

        if ("firefox".equals(browserType)) {
            matches = proxy.getHar().getLog().getEntries().stream()
                    .filter(entry -> entry.getRequest().getUrl().contains(partialUrl))
                    .map(entry -> entry.getResponse().getContent().getText())
                    .collect(Collectors.toList());

        } else {

            for (Map.Entry<String, String> entry : apiResponses.entrySet()) {
                if (entry.getKey().contains(partialUrl)) {
                    matches.add(entry.getValue());
                }
            }
        }
        if (matches.isEmpty()) {
            logger.warn("No API responses found for partial URL: ()", partialUrl);
        } else {
            logger.debug("Found {) matching responses for: ()", matches.size(), partialUrl);
        }
        System.out.println(matches);
        return matches;
    }

    public void stop() {
        if (proxy != null) {
            proxy.stop();
        }
    }
}



