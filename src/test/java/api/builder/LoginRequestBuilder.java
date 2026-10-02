package api.builder;

import api.model.request.LoginRequest;

public class LoginRequestBuilder {

    private LoginRequestBuilder() {
        // Private constructor to prevent instantiation
    }

    public static LoginRequest build(String userEmail, String userPassword) {

        LoginRequest request = new LoginRequest();
        request.userEmail = userEmail;
        request.userPassword = userPassword;

        return request;
    }
}
