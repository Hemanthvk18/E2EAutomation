package api.client;

import api.https.BaseApiClient;
import api.model.request.AddToCartRequest;
import api.model.response.AddToCartResponse;
import io.restassured.response.Response;

public class AddToCartClient extends BaseApiClient {


    private String addToCartEndpoint() {
        return "/api/ecom/user/add-to-cart";
    }

    public AddToCartResponse addToCart(AddToCartRequest request) {

        try {
            Response response = postJson(addToCartEndpoint(), request);

            validateSuccessfulResponse(response, "Add to Cart");

            return response.as(AddToCartResponse.class);

        } catch (Exception e) {
            throw new RuntimeException("Add to cart request failed: " + e.getMessage(), e);
        }
    }
}
