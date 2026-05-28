package api.client;

import api.payloads.request.LoginRequest;
import api.payloads.response.LoginResponse;

import io.restassured.http.ContentType;
import utilities.ConfigReader;

import static io.restassured.RestAssured.given;

public class AuthApiClient {

    private final String baseUrl = ConfigReader.getConfigReader().getBaseUrl();

    public LoginResponse login(LoginRequest request) {
        //Rest assured sees LoginRequest object, it will automatically convert it to JSON format and send it in the request body

        return
                given()
                        .baseUri(baseUrl)
                        .contentType(ContentType.JSON)
                        .body(request)                      //SERIALIZATION (Java Object → JSON)
                        .log().all()

                        .when()
                        .post("/api/ecom/auth/login")

                        .then()
                        .statusCode(200)
                        .log().all()
                        .extract()
                        .as(LoginResponse.class);            // DESERIALIZATION (JSON → Java Object) --> RESPONSE MAPPING
    }
}

/*
 LoginRequest object
 ↓
 RestAssured Serialization
 (Java → JSON)
 ↓
 API Request Sent
 ↓
 API Response JSON
 ↓
 RestAssured Deserialization
 (JSON → Java)
 ↓
 LoginResponse object
 ↓
 response.getToken()
 ↓
 Store in Context
 ↓
 Inject into Browser

 */

