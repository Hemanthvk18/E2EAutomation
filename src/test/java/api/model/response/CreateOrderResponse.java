package api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateOrderResponse {

    public ArrayList<String> orders;
    public ArrayList<String> productOrderId;
    public String message;

}
