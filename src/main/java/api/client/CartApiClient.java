package api.client;

import api.payloads.request.AddToCartRequest;
import api.payloads.response.AddToCartResponse;

import io.restassured.http.ContentType;
import utilities.ConfigReader;

import static io.restassured.RestAssured.given;

public class CartApiClient {

    private final String baseUrl = ConfigReader.getConfigReader().getBaseUrl();

    public AddToCartResponse addToCart(
            AddToCartRequest request,
            String token) {

        return
                given()
                        .baseUri(baseUrl)
                        .header("Authorization", token)
                        .contentType(ContentType.JSON)
                        .body(request)
                        .log().all()

                        .when()
                        .post("/api/ecom/user/add-to-cart")

                        .then()
                        .statusCode(200)
                        .log().all()
                        .extract()
                        .as(AddToCartResponse.class);
    }
}
