package api.client;

import api.payloads.request.CreateOrderRequest;
import api.payloads.response.CreateOrderResponse;
import io.restassured.http.ContentType;
import utilities.ConfigReader;

import static io.restassured.RestAssured.given;

public class OrderApiClient {

    private final String baseUrl = ConfigReader.getConfigReader().getBaseUrl();

    public CreateOrderResponse createOrder(
            CreateOrderRequest request,
            String token) {
        return
                given()
                        .baseUri(baseUrl)
                        .header("Authorization", token)
                        .contentType(ContentType.JSON)
                        .body(request)
                        .log().all()

                        .when()
                        .post("/api/ecom/order/create-order")

                        .then()
                        .statusCode(201)
                        .log().all()
                        .extract()
                        .as(CreateOrderResponse.class);

    }


}
