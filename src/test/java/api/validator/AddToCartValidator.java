package api.validator;

import api.model.response.AddToCartResponse;
import org.testng.Assert;

public class AddToCartValidator {
    public static void validate(AddToCartResponse response) {

        Assert.assertNotNull(response, "Response is null");

        Assert.assertNotNull(response.message, "Message missing");
    }
}

