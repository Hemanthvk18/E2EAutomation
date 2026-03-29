package utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

public class ConfigReader {
    private final static Logger logger = LoggerFactory.getLogger(ConfigReader.class);
    private static ConfigReader configReader;
    private Properties prop;

    public static synchronized ConfigReader getConfigReader() {
        try {
            if (Objects.isNull(configReader)) {
                configReader = new ConfigReader();
                configReader.loadProperties();
            }
        } catch (Exception e) {
            logger.error("Exception occured while loading properties", e.getMessage());
            e.printStackTrace();
        }
        return configReader;
    }

    public void loadProperties() throws IOException {
        try {
            prop = new Properties();
            FileReader file = new FileReader("src/main/resources/config.properties");
            prop.load(file);
        } catch (IOException e) {
            throw new IOException("Error loading configuration file", e);
        }
    }

    public String getProperty(String propertyName) {
        Objects.requireNonNull(propertyName, "propertyName must not be null");
        String value = System.getProperty(propertyName); // Check system properties first
        if (value == null) {
            value = System.getenv(propertyName); // Check environment variables
        }
        if (value == null) {
            value = prop.getProperty(propertyName); // Fallback to properties file
        }
        if (value == null) {
            logger.debug("Property '{}' not found in system/env/file", propertyName);
        }
        return value;
    }

    public String getProperty(String propertyName, String defaultValue) {
        String value = System.getProperty(propertyName); // Check system properties first
        if (value == null) {
            value = prop.getProperty(propertyName); // Fallback to properties file
        }
        if (value == null) {
            value = defaultValue;
            logger.warn("Property '" + propertyName + " ' is missing, defaulting to:" + defaultValue);
        }
        return value;
    }


    public String getUrl() {
        return getProperty("url");
    }

    public String getEmail(String userKey) {
//        return getProperty(userKey + "_EMAILID");
                return getProperty(userKey);

    }

    public String getPass(String userKey) {
//        return getProperty(userKey + "_PASS");
        return getProperty(userKey);

    }

    public String getSecretKey(String userKey) {
        return getProperty(userKey + "_SECRET_KEY");
    }

    public String getUserType(String userKey) {
        return getProperty(userKey + "_TYPE", "test"); // Default to test user
    }

}




