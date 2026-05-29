package managers;

import api.services.CartService;
import api.services.OrderService;
import pages.*;

public class PageObjectManager {
    TestContextManager context;
    private LoginPage loginPage;
    private HomePage homePage;
    private CartPage cartPage;
    private PaymentPage paymentPage;
    private ConfirmPage confirmPage;
    private CartService CartService;
    private OrderService orderService;

    public PageObjectManager(TestContextManager context) {
        this.context = context;
    }

    public LoginPage getLoginPage() {
        return (loginPage == null) ? loginPage = new LoginPage(context) : loginPage;
    }

    public HomePage getHomePage() {
        return (homePage == null) ? homePage = new HomePage(context) : homePage;
    }

    public CartPage getCartPage() {
        return (cartPage == null) ? cartPage = new CartPage(context) : cartPage;
    }

    public PaymentPage getPaymentPage() {
        return (paymentPage == null) ? paymentPage = new PaymentPage(context) : paymentPage;
    }

    public ConfirmPage getConfirmPage() {
        return (confirmPage == null) ? confirmPage = new ConfirmPage(context) : confirmPage;
    }
    public CartService getCartService() {
        return (CartService == null) ? CartService = new CartService() : CartService;
    }
    public OrderService getOrderService() {
        return (orderService == null) ? orderService = new OrderService() : orderService;
    }


}





