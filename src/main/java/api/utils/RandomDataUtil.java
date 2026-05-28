package api.utils;

import java.util.UUID;

public class RandomDataUtil {

    public static String generateCartId() {

        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 24);
    }

    public static String generatePolicyNumber() {

        return "POL" +
                System.currentTimeMillis();
    }

    public static String generateCustomerName() {

        return "Customer_" +
                System.currentTimeMillis();
    }
}
