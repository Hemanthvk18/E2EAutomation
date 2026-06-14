package api.services;

import api.client.OrderApiClient;
import api.payloads.request.CreateOrderRequest;
import api.payloads.request.Orders;
import api.payloads.response.CreateOrderResponse;
import api.utils.OderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderApiClient orderApiClient = new OrderApiClient();

    private final OderRepository oderRepository = new OderRepository();

    public CreateOrderResponse createOrder(
            String token,
            List<String> productNames) {

        List<Orders> ordersList = new ArrayList<>();

        for (String productName : productNames) {

            Orders order = oderRepository.getOrderByName(productName);
            ordersList.add(order);
        }

        CreateOrderRequest request = new CreateOrderRequest();
        request.setOrders(ordersList);

        // API call
        CreateOrderResponse response = orderApiClient.createOrder(request, token);
        System.out.println("Order Response : " + response.getMessage());
        return response;

    }


}
