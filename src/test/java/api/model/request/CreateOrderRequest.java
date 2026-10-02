package api.model.request;

import api.model.common.Orders;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateOrderRequest {

    public Orders orders;

//    public List<String> order=new ArrayList<>();
//
//
//
//
//    public CreateOrderRequest createOrder(String... productIds) {
//        for (String productId : productIds) {
//            this.order.add(productId);
//        }
//        return this;
//
//    }



}
