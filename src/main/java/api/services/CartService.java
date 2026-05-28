package api.services;


import api.client.CartApiClient;
import api.payloads.request.AddToCartRequest;
import api.payloads.request.Product;
import api.payloads.response.AddToCartResponse;
import api.utils.ProductRepository;
import api.utils.RandomDataUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CartService {
    private static final Logger logger = LoggerFactory.getLogger(CartService.class);


    private final CartApiClient cartApiClient =
            new CartApiClient();

    private final ProductRepository productRepository =
            new ProductRepository();

    public String addProductToCart(
            String token,
            String userId,
            String productName) {

        // Get product dynamically
        Product product = productRepository.getProductByName(productName);

        // Build request
        AddToCartRequest request = new AddToCartRequest();
        request.set_id(userId);
        request.setProduct(product);

        // API call
        AddToCartResponse response = cartApiClient.addToCart(request, token);

        System.out.println("Cart Response : " + response.getMessage());
        return response.getMessage();
    }
}
