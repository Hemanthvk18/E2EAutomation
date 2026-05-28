package api.http;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.Filter;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public final class RestAssuredConfigFactory {

    private static final ThreadLocal<RequestSpecification> SPEC = new ThreadLocal<>();

    public static RequestSpecification get(String baseUrl, Duration timeout, boolean logOnFailureOnly) {
        RequestSpecification spec = SPEC.get();
        if (spec == null) {
            List<Filter> filters = new ArrayList<>();
            filters.add(Filters.retryFilter(3, 300, new int[]{429, 500, 502, 503, 504}));

            if (!logOnFailureOnly) {
                filters.add(new RequestLoggingFilter());
                filters.add(new ResponseLoggingFilter());
            } else {
                RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
            }

            spec = new RequestSpecBuilder()
                    .setBaseUri(baseUrl)
                    .setRelaxedHTTPSValidation()
                    .addFilters(filters)
                    // Default timeouts: use JVM http.* or if you use Apache HTTP Client, set there.
                    .build();

            SPEC.set(spec);
        }
        return spec;
    }

    public static void clear() {
        SPEC.remove();
    }

}


