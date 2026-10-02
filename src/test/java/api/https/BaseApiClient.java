package api.https;

import api.model.request.LoginRequest;
import auth.AuthContext;
import io.restassured.http.ContentType;
import utilities.ConfigReader;
import io.restassured.response.Response;

import io.restassured.specification.RequestSpecification;

import java.io.File;

import java.util.Map;

import static io.restassured.RestAssured.given;

//Common helpers for all API clients.

public abstract class BaseApiClient {

    protected final ConfigReader cfg;
    private final String baseUrl = ConfigReader.getConfigReader().getBaseUrl();

    protected BaseApiClient() {
        this.cfg = ConfigReader.getConfigReader();
    }


    public Response postLogin(String path, LoginRequest request) {
        //Rest assured sees LoginRequest object, it will automatically convert it to JSON format and send it in the request body

        return
                given()
                        .spec(RestAssuredConfigFactory.get(cfg.getUrl(), cfg.httpTimeout(), cfg.logonFailureOnly()))
                        .baseUri(baseUrl)
                        .contentType(ContentType.JSON)
                        .body(request)                      //SERIALIZATION (Java Object → JSON)
                        .log().all()

                        .when()
                        .post(path)

                        .then()
                        .extract().response();
  }

    protected Response postJson(String path, Object payload) {

        String token = AuthContext.getBearerToken();

        if (token == null) {

            throw new IllegalStateException(
                    "Bearer token is null for thread: "
                            + Thread.currentThread().getName());
        }

        return given()
                .spec(RestAssuredConfigFactory.get(cfg.getUrl(), cfg.httpTimeout(), cfg.logonFailureOnly()))
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(payload)
                .post(path)
                .then()
                .extract().response();

    }

    /**
     * For endpoints expecting multipart, optionally with JSON field named "data" and files.
     */

    protected Response postMultipart(
            String path,
            Map<String, String> textParts,
            Map<String, File> fileParts

    ) {
        Response response = null;

        RequestSpecification spec = given()
                .spec(RestAssuredConfigFactory.get(
                        cfg.getUrl(),
                        cfg.httpTimeout(),
                        cfg.logonFailureOnly()
                ))
                .header("Authorization", "Bearer" + AuthContext.getBearerToken());

        if (textParts != null) {
            textParts.forEach(spec::multiPart);
        }

        if (fileParts != null) {
            fileParts.forEach(spec::multiPart);
        }

        try {
            response =
                    given(spec)
                            .when()
                            .post(path);

        } catch (Exception e) {
            System.out.println(
                    "POST FAILED THREAD:"
                            + Thread.currentThread().getName());

            e.printStackTrace();
            throw e;
        }

        System.out.println(
                "RESPONSE OBJECT: "
                        + response);

        if (response != null) {
            System.out.println("STATUS CODE: "

                    + response.statusCode());
        }

        return response;
    }

    protected void validateSuccessfulResponse(
            Response response,
            String operation) {

        if (response == null) {
            throw new RuntimeException(operation + " returned null response");
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {

            throw new RuntimeException(
                    operation
                            + "failed.Status = "
                            + response.statusCode()
                            + ", response="
                            + response.asString());

        }

    }

}