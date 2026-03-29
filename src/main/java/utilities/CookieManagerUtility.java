package utilities;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class CookieManagerUtility {
    public static final String COOKIE_FILE = "cookies" + Thread.currentThread().getId() + ".json";
    private static ThreadLocal<Set<Cookie>> threadLocalCookies = ThreadLocal.withInitial(() -> null);

    public static void saveCookies(WebDriver driver) {
        Set<Cookie> cookies = driver.manage().getCookies();
        JSONArray cookieList = new JSONArray();
        for (Cookie cookie : cookies) {
            JSONObject cookieObject = new JSONObject();
            cookieObject.put("name", cookie.getName());
            cookieObject.put("value", cookie.getValue());
            cookieObject.put("domain", cookie.getDomain());
            cookieObject.put("path", cookie.getPath());
            cookieObject.put("expiry", cookie.getExpiry() != null ? cookie.getExpiry().getTime() : null);
            cookieObject.put("secure", cookie.isSecure());
            cookieObject.put("httpOnly", cookie.isHttpOnly());
            cookieList.put(cookieObject);
        }

        try (FileWriter file = new FileWriter(COOKIE_FILE)) {
            file.write(cookieList.toString());
        } catch (IOException e) {

            e.printStackTrace();
        }
        threadLocalCookies.set(cookies);
    }

    public static void saveSessionData(WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // Save session ID from session storage
        String sessionId = (String) js.executeScript("return sessionStorage.getItem('sessionId');");

        // Save cookies
        Set<Cookie> cookies = driver.manage().getCookies();

        // Store in a file
        try (FileWriter writer = new FileWriter("sessionData.txt")) {
            writer.write("SessionID=" + sessionId + "\n");
            for (Cookie cookie : cookies) {
                writer.write(cookie.getName() + "=" + cookie.getValue() + ";" + cookie.getDomain() + "\n");
            }
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public static void restoreSessionData(WebDriver driver) {
        try (BufferedReader reader = new BufferedReader(new FileReader("sessionData.txt"))) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("SessionID=")) {

                    // Restore session ID
                    String sessionId = line.split("=")[1];
                    js.executeScript("sessionStorage.setItem('sessionId', '" + sessionId + "');");
                } else {

                    // Restore cookies
                    String[] parts = line.split("=");
                    String name = parts[0];
                    String value = parts[1].split(";")[0];
                    String domain = parts[1].split(";")[1];

                    Cookie cookie = new Cookie.Builder(name, value).domain(domain).path("/").isSecure(true).build();
                    driver.manage().addCookie(cookie);
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveCookies(WebDriver driver, String path) throws IOException {
        Set<Cookie> cookies = driver.manage().getCookies();
        for (Cookie cookie : cookies) {
            FileWriter writer = new FileWriter(path);
            writer.write(cookie.getName() + "=" + cookie.getValue());
            writer.close();
        }
    }

    public static void LoadCookies(WebDriver driver, String path) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(path));
        String line = reader.readLine();
        reader.close();
        if (line != null) {
            String[] cookies = line.split("=");
            if (cookies.length == 2) {
                Cookie sessionCookie = new Cookie(cookies[0], cookies[1]);
                driver.manage().addCookie(sessionCookie);
            }
        }
    }

    public static void loadCookies(WebDriver driver) {
        File cookieFile = new File(COOKIE_FILE);
        if (cookieFile.exists()) {
            try {
                String content = new String(java.nio.file.Files.readAllBytes(cookieFile.toPath()));
                JSONArray cookieList = new JSONArray(content);
                for (int i = 0; i < cookieList.length(); i++) {
                    JSONObject cookieObject = cookieList.getJSONObject(i);
                    Long expiryMillis = cookieObject.isNull("expiry") ? null : cookieObject.getLong("expiry");
                    java.util.Date expiryDate = expiryMillis != null ? new java.util.Date(expiryMillis) : null;
                    Cookie cookie = new Cookie(cookieObject.getString("name"), cookieObject.getString("value"),
                            cookieObject.getString("domain"), cookieObject.getString("path"), expiryDate,
                            cookieObject.getBoolean("secure"), cookieObject.getBoolean("httpOnly"));
                    driver.manage().addCookie(cookie);
                }
            } catch (IOException | org.json.JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public static void clearCookies() {
        threadLocalCookies.remove();
    }

    public static void extendCookies(WebDriver driver) {
        for (Cookie cookie : driver.manage().getCookies()) {
            Cookie extendCookie = new Cookie.Builder(cookie.getName(), cookie.getValue()).domain(cookie.getDomain())
                    .path(cookie.getPath()).isSecure(cookie.isSecure()).isHttpOnly(cookie.isHttpOnly())
                    .expiresOn(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(5))).build();
            driver.manage().deleteCookieNamed(cookie.getName());
            driver.manage().addCookie(extendCookie);
        }

    }

}

