package api.service;

import api.builder.CreateOrderRequestBuilder;
import api.client.CreateOrderClient;
import api.constants.ContextKeys;
import api.model.request.CreateOrderRequest;
import api.model.response.AddToCartResponse;
import api.model.response.CreateOrderResponse;
import api.validator.CreateOrderValidator;
import managers.TestContextManager;
import utilities.AllureUtility;

import java.util.List;

public class CreateOrderService {

    private final TestContextManager context;

    private final CreateOrderClient client = new CreateOrderClient();

    public CreateOrderService(TestContextManager context) {
        this.context = context;
    }

    public void createOrder(String country, String productId) {

        CreateOrderRequest request = CreateOrderRequestBuilder.build(country, productId);

        CreateOrderResponse response= client.createOrder(request);

        CreateOrderValidator.validate(response);

        addAllureReport(response,productId);

        saveContext(response, productId);



    }

    private void addAllureReport(CreateOrderResponse response,String productId) {
        AllureUtility.addSubStepForData("Order Status",
                "Message", response.message);

        AllureUtility.addSubStepForData("Order Status",
                "Order ID", String.valueOf(response.orders));


    }

    private void saveContext(CreateOrderResponse response, String productId) {
        context.put(ContextKeys.ORDER_ID, response.orders);
    }
}
