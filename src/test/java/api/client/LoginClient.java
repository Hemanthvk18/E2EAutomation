package api.client;

import api.https.BaseApiClient;
import api.model.request.LoginRequest;
import api.model.response.LoginResponse;
import io.restassured.response.Response;
import utilities.ConfigReader;

public class LoginClient extends BaseApiClient {

    private final String baseUrl = ConfigReader.getConfigReader().getBaseUrl();


    private String loginEndpoint() {
        return "/api/ecom/auth/login";
    }

    public LoginResponse login(LoginRequest request) {

        try {
            Response response = postLogin(loginEndpoint(), request);

            validateSuccessfulResponse(response, "Login");

            return response.as(LoginResponse.class); // DESERIALIZATION (JSON → Java Object) --> RESPONSE MAPPING

        } catch (Exception e) {
            throw new RuntimeException("Login request failed: " + e.getMessage(), e);
        }

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

