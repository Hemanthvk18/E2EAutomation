package api.builder;

import api.model.request.AddToCartRequest;

public class AddToCartRequestBulider {

    private AddToCartRequestBulider() {
        // Private constructor to prevent instantiation
    }

    public static AddToCartRequest build(String userID, String productId) {
        AddToCartRequest request = new AddToCartRequest();
        request._id = userID;
        request.product._id = productId;

        return request;
    }
}
