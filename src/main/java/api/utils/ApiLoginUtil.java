package api.utils;


import io.restassured.RestAssured;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class ApiLoginUtil {

    public static String generateToken(String email, String password) {

        // Base URI
        RestAssured.baseURI = "https://rahulshettyacademy.com";

        // Request Body
        String requestBody = "{\n" +
                "    \"userEmail\": \"" + email + "\",\n" +
                "    \"userPassword\": \"" + password + "\"\n" +
                "}";

        // API Call
        Response response =
                given()
                        .header("Content-Type", "application/json")
                        .body(requestBody)

                        .when()
                        .post("/api/ecom/auth/login")

                        .then()
                        .extract()
                        .response();

        // Print response
        System.out.println("Login Response : " + response.asPrettyString());

        // Extract token
        String token = response.jsonPath().getString("token");

        System.out.println("Generated Token : " + token);

        return token;
    }
}
