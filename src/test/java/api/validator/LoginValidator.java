package api.validator;

import api.model.response.LoginResponse;
import org.testng.Assert;

public class LoginValidator {

    public static void validate(LoginResponse response) {

        Assert.assertNotNull(response, "Response is null");

        Assert.assertNotNull(response.token, "Token missing");

        Assert.assertNotNull(response.userId, "User ID missing");

        Assert.assertNotNull(response.message, "Message missing");
    }
}