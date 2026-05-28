package utilities;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class TokenExtractor {

    /**
     * Polls the Network CaptureUtil for a response body containing the token.
     *
     * @param netUtil        existing NetworkCaptureUtil instance (already attached to driver)
     * @param partialUrl     a unique part of the token API URL (e.g. "/get-token" or "get-token")
     * @param jsonkey        the JSON key to extract (e.g. "token" or "access_token")
     * @param timeoutSeconds max seconds to wait
     * @return token string if found, else throws IllegalStateException
     */

    public static String waitAndExtractToken(NetworkCaptureUtil netUtil,
                                             String partialUrl,
                                             String jsonkey,
                                             int timeoutSeconds) {

        Instant start = Instant.now();
        String token = null;

        while (Duration.between(start, Instant.now()).getSeconds() < timeoutSeconds) {
            List<String> matches = netUtil.getAllApiResponses(partialUrl);
            for (String body : matches) {
                if (body == null || body.isEmpty()) continue;
                // Some devtools return base64-encoded responses for binary; your code
                // already uses body as text; if you ever get base64 flag you'd decode here.
                token = JsonUtil.getValueByKey(body, jsonkey);
                if (token != null && !token.isBlank()) {
                    return token;
                }

            }
            sleep(300); // small backoff
        }
        throw new IllegalStateException("Timed out waiting for token from '" + partialUrl + "'");

    }


    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }

    }
}

