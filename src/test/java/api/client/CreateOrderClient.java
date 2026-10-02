package api.client;

import api.https.BaseApiClient;
import api.model.request.CreateOrderRequest;
import api.model.response.CreateOrderResponse;
import io.restassured.response.Response;

public class CreateOrderClient extends BaseApiClient {

    private String createOrderEndpoint() {
        return "/api/ecom/order/create-order";
    }

    public CreateOrderResponse createOrder(CreateOrderRequest request) {

        try {
            Response response = postJson(createOrderEndpoint(), request);

            validateSuccessfulResponse(response, "Create Order");

            return response.as(CreateOrderResponse.class);

        } catch (Exception e) {
            throw new RuntimeException("Create order request failed: " + e.getMessage(), e);
        }
    }

}
