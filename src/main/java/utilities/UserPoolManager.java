package utilities;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class UserPoolManager {
    private final Map<String, BlockingQueue<String>> userPools = new ConcurrentHashMap<>();
    private final ConfigReader configReader;

    public UserPoolManager(ConfigReader configReader) {
        this.configReader = configReader;
        initializePools();
    }

    private void initializePools() {
// Supported categories
        String[] categories = {"normal_admin", "test_admin", "test_normal"};
        for (String category : categories) {
            String[] userKeys = configReader.getProperty(category + ".users", "").split(",");
            if (userKeys.length > 0 && !userKeys[0].isEmpty()) {
                BlockingQueue<String> queue = new LinkedBlockingQueue<>();
                for (String userKey : userKeys) {
                    if (!userKey.trim().isEmpty()) {
                        queue.add(userKey.trim());
                    }
                }
                userPools.put(category, queue);

            }

        }

    }


    public String acquireUser(String role) throws InterruptedException {
        BlockingQueue<String> pool = userPools.get(role.toLowerCase());
        if (pool == null) {
            throw new IllegalArgumentException("No user pool for role: " + role);
        }
        return pool.take();
    }

    public void releaseUser(String role, String userkey) {
        BlockingQueue<String> pool = userPools.get(role.toLowerCase());
        if (pool != null) {
            pool.add(userkey);  // Return user back to pool for reuse by other tests
        }

    }


}
