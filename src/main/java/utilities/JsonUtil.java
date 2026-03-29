package utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;

public class JsonUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    /**
     * Extracts the first matching value for a given key from a JSON string.
     *
     * @param json The JSON string
     * @param key  The key to search for
     * @return The value as a string, or null if not found
     */

    public static String getValueByKey(String json, String key) {
        if (json == null || json.isEmpty()) {
            logger.warn("Empty or null JSON input.");
            return null;
        }
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            String value = findValue(rootNode, key);
            if (value == null) {
                logger.debug("Key '{}' not found in JSON.", key);
            }
            return value;
        } catch (Exception e) {
            logger.error("Error parsing JSON: {}", e.getMessage());
            return null;
        }
    }


    private static String findValue(JsonNode node, String key) {
        if (node == null) return null;
        if (node.has(key)) {
            return node.get(key).asText();
        }
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String value = findValue(field.getValue(), key);
                if (value != null) return value;
            }
        } else if (node.isArray()) {
            for (JsonNode arrayItem : node) {
                String value = findValue(arrayItem, key);
                if (value != null) return value;
            }
        }
        return null;
    }
}
