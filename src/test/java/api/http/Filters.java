package api.http;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

public class Filters {

    public static Filter retryFilter(int maxRetries, long backoffMs, int[] retryOnStatus) {
        return (Filter) (FilterableRequestSpecification req,
                         FilterableResponseSpecification res,
                         FilterContext ctx) -> {

            int attempt = 0;

            while (true) {
                Response response = null;

                try {
                    response = ctx.next(req, res); // may return null
                } catch (Exception ignored) {
                    // swallow and retry
                }

                int sc = (response != null) ? response.statusCode() : -1; // null-safe

                //if no retry OR max retries hit return whatever we have
                if (!shouldRetry(sc, retryOnStatus) || attempt >= maxRetries) {
                    return response; // might be null, caller must handle

                }

                attempt++;

                sleep(backoffMs * attempt);

            }
        };
    }

    private static boolean shouldRetry(int status, int[] retryOn) {
        for (int s : retryOn)
            if (s == status) return true;
        return false;
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {

        }
    }
}