package api.payloads.request;

import org.apache.commons.math3.geometry.partitioning.BSPTree;

import java.util.List;

public class CreateOrderRequest {


    private List<Orders> orders;

    public List<Orders> getOrders() {
        return orders;
    }

    public void setOrders(List<Orders> orders) {
        this.orders = orders;
    }


}
