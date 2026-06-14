package api.utils;

import api.payloads.request.Orders;

import java.util.HashMap;
import java.util.Map;

public class OderRepository {

    private static final Map<String, Orders> OrderData = new HashMap<>();

    static {

        // PRODUCT 1
        Orders adidas = new Orders();
        adidas.setCountry("India");
        adidas.setProductOrderedId("6960eae1c941646b7a8b3ed3");

        OrderData.put("ADIDAS ORIGINAL", adidas);

        // PRODUCT 2
        Orders zara = new Orders();
        zara.setCountry("India");
        zara.setProductOrderedId("6960eac0c941646b7a8b3e68");

        OrderData.put("ZARA COAT 3", zara);

        // PRODUCT 3
        Orders iphone = new Orders();
        iphone.setCountry("India");
        iphone.setProductOrderedId("6960ea76c941646b7a8b3dd5");

        OrderData.put("iphone 13 pro", iphone);

    }

    public Orders getOrderByName(String productName) {

        Orders order = OrderData.get(productName);

        if (order == null) {

            throw new RuntimeException("Order not found : " + productName);
        }

        return order;
    }
}
