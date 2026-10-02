package api.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;

public final class ApiLogHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ApiLogHelper() {
    }

    public static void logRequest(String apiName,
                                  Object request) {

        try {

            System.out.println("============ " + apiName + "  REQUEST ============");

            System.out.println(
                    MAPPER
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(request));

            System.out.println("==================================== ");

        } catch (Exception e) {
            System.out.println("Unable to print API request");
        }

    }

    public static void logResponse(String apiName, Response response) {

        System.out.println("==========" + apiName + "RESPONSE ==========");

        System.out.println("Status Code: " + response.statusCode());

        System.out.println(response.asPrettyString());

        System.out.println("==================================== ");

    }

}


