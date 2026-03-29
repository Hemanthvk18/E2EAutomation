package utilities;

import java.io.InputStream;
import java.util.Properties;

public class EnvResourceResolver {
    private final Properties properties = new Properties();

    public EnvResourceResolver(String resourceType) {
        String env = ConfigReader.getConfigReader().getProperty("env", "dev");
        String fileName = String.format("%s-%s.properties", resourceType, env);

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                throw new RuntimeException("Resource file not found: " + fileName);
            }
            properties.load(input);
        } catch (Exception e) {
            throw new RuntimeException("Could not load resource properties file: " + fileName, e);
        }
    }

    public String getValue(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new RuntimeException("Value not found for key: " + key);
        }
        return value;
    }
}
