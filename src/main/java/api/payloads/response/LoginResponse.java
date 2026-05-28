package api.payloads.response;

public class LoginResponse {

    private String token;
    private String userId;
    private String message;

    //ex : response.token = "abc123xyz" --> response.getToken() --> "abc123xyz"

    public String getToken() {
        return token;
    }

    public String getUserId() {
        return userId;
    }

    public String getMessage() {
        return message;
    }
}
