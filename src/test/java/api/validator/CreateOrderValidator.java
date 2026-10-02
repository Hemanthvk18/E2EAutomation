package api.validator;

import api.model.response.AddToCartResponse;
import api.model.response.CreateOrderResponse;
import org.testng.Assert;

public class CreateOrderValidator {

    public static void validate(CreateOrderResponse response) {

        Assert.assertNotNull(response, "Response is null");

        Assert.assertNotNull(response.message, "Message missing");

        Assert.assertNotNull(response.orders, "Order missing");

        Assert.assertNotNull(response.productOrderId, "Product ID missing");

    }
}
