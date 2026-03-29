package utilities;


import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserCapabilityMapper {
    private final Map<String, List<String>> capabilityMap = new HashMap<>();
    private final ConfigReader configReader;

    public UserCapabilityMapper(ConfigReader configReader) {
        this.configReader = configReader;
        initializeCapabilities();
    }

    private void initializeCapabilities() {
        // Define capabilities and which categories satisfy them
        capabilityMap.put("admin", Arrays.asList("normal_admin", "test_admin"));
        capabilityMap.put("normal_user", Collections.singletonList("test_normal"));

        // Special capabilities
        capabilityMap.put("normal_admin_only", Collections.singletonList("normal_admin"));
    }

    public List<String> getCategoriesForCapability(String capability) {
        return capabilityMap.getOrDefault(capability, Collections.emptyList());
    }

    public String getDefaultCategoryForCapability(String capability) {
        return configReader.getProperty(capability + ".default.category", "");
    }

}
