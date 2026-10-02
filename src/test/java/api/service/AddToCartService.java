package api.service;

import api.builder.AddToCartRequestBulider;
import api.client.AddToCartClient;
import api.constants.ContextKeys;
import api.model.request.AddToCartRequest;
import api.model.response.AddToCartResponse;
import api.model.response.LoginResponse;
import api.validator.AddToCartValidator;
import api.validator.LoginValidator;
import managers.TestContextManager;
import utilities.AllureUtility;

import static api.helper.ApiLogHelper.logRequest;

public class AddToCartService {

    private final TestContextManager context;

    private final AddToCartClient client = new AddToCartClient();

    public AddToCartService(TestContextManager context) {
        this.context = context;
    }

    public void addToCart(String userID, String productId) {

        AddToCartRequest request = AddToCartRequestBulider.build(userID, productId);

        logRequest("Add to Cart",request);

        AddToCartResponse response = client.addToCart(request);

        AddToCartValidator.validate(response);

        addAllureReport(response);

        saveContext(response, productId);

    }

    private void addAllureReport(AddToCartResponse response) {
        AllureUtility.addSubStepForData("Add To Cart Status",
                "Message", response.message);
    }

    private void saveContext(AddToCartResponse response, String productId) {
        context.put(ContextKeys.PRODUCT_ID, productId);
    }
}
