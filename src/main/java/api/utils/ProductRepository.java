package api.utils;

import api.payloads.request.Product;

import java.util.HashMap;
import java.util.Map;

public class ProductRepository {

    private static final Map<String, Product> productData = new HashMap<>();

    static {

        // PRODUCT 1
        Product adidas = new Product();

        adidas.set_id("6960eae1c941646b7a8b3ed3");
        adidas.setProductName("ADIDAS ORIGINAL");
//        adidas.setProductCategory("electronics");
//        adidas.setProductSubCategory("mobiles");
//        adidas.setProductPrice(11500);
        productData.put("ADIDAS ORIGINAL", adidas);

        // PRODUCT 2
        Product zara = new Product();

        zara.set_id("6960eac0c941646b7a8b3e68");
        zara.setProductName("ZARA COAT 3");
//        zara.setProductCategory("electronics");
//        zara.setProductSubCategory("mobiles");
//        zara.setProductPrice(11500);
        productData.put("ZARA COAT 3", zara);

        // PRODUCT 3
        Product iphone = new Product();

        iphone.set_id("6960ea76c941646b7a8b3dd5");
        iphone.setProductName("iphone 13 pro");
//        iphone .setProductCategory("electronics");
//        iphone .setProductSubCategory("mobiles");
//        iphone .setProductPrice(55000);
        productData.put("iphone 13 pro", iphone);


    }

    public Product getProductByName(String productName) {

        Product product = productData.get(productName);

        if (product == null) {

            throw new RuntimeException("Product not found : " + productName);
        }

        return product;
    }
}
