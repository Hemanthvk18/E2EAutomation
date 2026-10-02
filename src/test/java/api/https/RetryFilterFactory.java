package api.https;

import io.restassured.filter.Filter;
import io.restassured.response.Response;

public class RetryFilterFactory {

    public static Filter retryFilter(
            int maxRetries,
            long backoffMs,
            int[] retryOnStatus) {

        return (req, res, ctx) -> {

            int attempt = 0;

            while (true) {

                try {

                    System.out.println("Retry Filter Thread: " + Thread.currentThread().getName());

                    System.out.println("Request URI: " + req.getURI());

                    System.out.println("Retry Attempt: " + attempt);

                    Response response = ctx.next(req, res);

                    if (response == null) {

                        throw new RuntimeException("Response is null");

                    }

                    int sc = response.statusCode();

                    if (!shouldRetry(
                            sc,
                            retryOnStatus)
                            || attempt >= maxRetries) {

                        return response;
                    }


                } catch (Exception e) {

                    System.out.println(
                            "Retry Attempt "
                                    + attempt
                                    + "failed: "
                                    + e.getMessage());

                    if (attempt >= maxRetries) {
                        throw e;
                    }

                }
                attempt++;
                sleep(backoffMs * attempt);

            }

        };

    }

    private static boolean shouldRetry(int status, int[] retryon) {
        for (int s : retryon) if (s == status) return true;
        return false;
    }


    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {

        }
    }
}