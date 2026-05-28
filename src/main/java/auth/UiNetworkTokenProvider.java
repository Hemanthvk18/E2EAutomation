package auth;


import org.openqa.selenium.WebDriver;
import utilities.NetworkCaptureUtil;
import utilities.TokenExtractor;

public class UiNetworkTokenProvider implements AuthProvider {

    private final WebDriver driver;
    private final NetworkCaptureUtil net;
    private final String tokenApiPartial;
    private final String tokenKey;

    public UiNetworkTokenProvider(WebDriver driver,

                                  NetworkCaptureUtil networkUtil,
                                  String tokenApiPartial, // e.g. "/get-token"
                                  String tokenKey) { // e.g. "token" or "access_token"

        this.driver = driver;
        this.net = networkUtil;
        this.tokenApiPartial = tokenApiPartial;
        this.tokenKey = tokenKey;
    }

    @Override
    public void ensureAuthenticated() {
        // assumes you already logged in via UI before calling this
        String token = TokenExtractor.waitAndExtractToken(net, tokenApiPartial, tokenKey, 20);
        AuthContext.setBearerToken(token);

        // If your app also uses SESSIONID cookies:
        // Cookie c = ((ChromeDriver) driver).manage().getCookieNamed("SESSIONID");
        // if (c != null) AuthContext.setSessionId(c.getValue());
    }
}
