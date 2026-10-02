package utilities;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * UserPoolManager manages pools of user keys for different roles.
 * It allows acquiring and releasing user keys in a thread-safe manner.
 *
 * BlockingQueue is used to ensure that if a user key is not available, the thread will wait until one becomes available.
 * ConcurrentHashMap allows multiple threads to access the map safely
 * LinkedBlockingQueue ensures that each parallel test thread acquires a unique user in FIFO order.
 * Once a test finishes, the user can be returned to the queue for reuse, preventing conflicts during parallel execution.
 */


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

        String userKey = pool.take();  // This will block if no users are available

        System.out.println(
                Thread.currentThread().getName()
                        + " acquired "
                        + userKey);

        return userKey;
    }

    public void releaseUser(String role, String userKey) {
        BlockingQueue<String> pool = userPools.get(role.toLowerCase());
        if (pool != null) {
            pool.add(userKey);  // Return user back to pool for reuse by other tests
            System.out.println(
                    Thread.currentThread().getName()
                            + " released "
                            + userKey);
        }

    }


}
