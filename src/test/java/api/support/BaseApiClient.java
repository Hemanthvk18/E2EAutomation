package api.support;

import api.http.RestAssuredConfigFactory;
import auth.AuthContext;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import utilities.ConfigReader;

import java.io.File;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Common helpers for all API clients.
 */

public abstract class BaseApiClient {

    protected final ConfigReader cfg;

    protected BaseApiClient() {
        this.cfg = ConfigReader.getConfigReader();
    }


    protected Response postJson(String path, Object payload) {
        return given()
                .spec(RestAssuredConfigFactory.get(cfg.getUrl(), cfg.httpTimeout(), cfg.logonFailureOnly()))
                .header("Authorization", "Bearer " + AuthContext.getBearerToken())
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

        RequestSpecification spec =
                given()
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

        return given(spec)
                .when()
                .post(path)
                .then()
                .extract()
                .response();

    }
}