package api.model.request;

import api.model.common.Product;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddToCartRequest {

    public String _id;

    public Product product;


}
