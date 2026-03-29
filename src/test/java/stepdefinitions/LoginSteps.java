package stepdefinitions;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import managers.TestContextManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.LoginPage;

public class LoginSteps {
    private static final Logger logger= LoggerFactory.getLogger(LoginSteps.class);
    TestContextManager context;
    LoginPage loginPage;

    public LoginSteps(TestContextManager context)
    {
        this.context=context;
        this.loginPage=context.getPageObjectManager().getLoginPage();
    }


}
