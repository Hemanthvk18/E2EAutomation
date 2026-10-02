package api.builder;

import api.model.request.CreateOrderRequest;

import java.util.List;

public class CreateOrderRequestBuilder {

    private CreateOrderRequestBuilder() {
        // Private constructor to prevent instantiation
    }

    public static CreateOrderRequest build(String country, String productOrderedId) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.orders.country = country;
        request.orders.productOrderedId = productOrderedId;

        return request;
    }
}
