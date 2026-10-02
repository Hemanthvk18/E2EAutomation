package api.service;

import api.builder.LoginRequestBuilder;
import api.client.LoginClient;
import api.constants.ContextKeys;
import api.model.request.LoginRequest;
import api.model.response.LoginResponse;
import api.validator.LoginValidator;
import managers.TestContextManager;
import utilities.AllureUtility;

import static api.helper.ApiLogHelper.logRequest;

public class LoginService {
    private final TestContextManager context;
    private final LoginClient client = new LoginClient();

    public LoginService(TestContextManager context) {
        this.context = context;
    }

    public LoginResponse loginToApplication(String username, String password) {

        LoginRequest request = LoginRequestBuilder.build(username, password);

        logRequest("Login",request);

        LoginResponse response = client.login(request);

        // need to check
        context.setToken(response.token);
        context.setUserId(response.userId);

        LoginValidator.validate(response);

        addAllureReport(response);

        saveContext(response);

        return response;

    }


    private void addAllureReport(LoginResponse response) {
        AllureUtility.addSubStepForData("Login Status",
                "Message", response.message);
    }

    private void saveContext(LoginResponse response) {

        context.put(ContextKeys.BEARER_TOKEN, response.token);
        context.put(ContextKeys.USER_ID, response.userId);
    }

}
