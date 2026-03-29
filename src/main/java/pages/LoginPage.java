package pages;

import managers.TestContextManager;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.ConfigReader;

public class LoginPage {
    TestContextManager context;
    private static final Logger logger = LoggerFactory.getLogger(LoginPage.class);

//    @FindBy(id = "login")
//    WebElement loginButton;

    By loginButton=By.id("login");

    @FindBy(xpath = "")
    WebElement testUserEmailInput;

    @FindBy(xpath = "")
    WebElement testUserContinueButtonForUserID;

    @FindBy(xpath = "")
    WebElement testUserPasswordInput;

    @FindBy(xpath = "")
    WebElement testUserContinueButtonForpassword;

    @FindBy(xpath = "")
    WebElement authenticatorSelection;

    @FindBy(xpath = "")
    WebElement testUserLoginButton;

//    @FindBy(id = "userEmail")
//    WebElement emailInput;
    By emailInput=By.id("userEmail");

//    @FindBy(id = "userPassword")
//    WebElement passwordInput;
    By passwordInput=By.id("userPassword");


    @FindBy(xpath = "")
    WebElement otpInput;


    public LoginPage(TestContextManager context) {
        this.context = context;
    }


    public void loadLoginPageWithRetry(String url) {
        context.getCustomActions().loadPageWithRetry(url, loginButton, 3);
    }


    public boolean inputUserName(String emailId) {
        if (context.getCustomActions().customSendKeys(emailInput, "Microsoft Email Input", emailId)) {
//            context.getCustomActions().clickEnter(emailInput);
            return true;
        }
        return false;
    }


    public boolean inputPassword(String password) {
        if (context.getCustomActions().customSendKeys(passwordInput, "Microsoft Password Input", password)) {
//            context.getCustomActions().clickEnter(passwordInput);
            return true;
        }
        return false;
    }

    public boolean inputOtp(String otp) {
        if (context.getCustomActions().customSendKeys(otpInput, "Google MFA OTP", otp)) {
//            context.getCustomActions().clickEnter(otpInput);
            return true;

        }

        return false;

    }

    public boolean clickButton() {
        return context.getCustomActions().customClick(loginButton, "Login Button");
    }

    public void login(String emailId, String password) {
        boolean isEmailEntered = inputUserName(emailId);
        boolean isPasswordEntered = inputPassword(password);
        boolean isLoginClicked = clickButton();

            if (isEmailEntered && isPasswordEntered && isLoginClicked) {
                logger.info("Logged into application successfully.");
                return;
            } else {
                logger.error("One or more input steps failed. Login process could not be completed.");
                throw new RuntimeException("Login process failed due to input errors.");
            }
        }



//    public void adminLogin(String emailId, String password, String otpSecretKey) {
//        try {
////            String userToRun =ConfigReader.getConfigReader().getUserToRun();
////            String passWord =ConfigReader.getConfigReader().getPass(userToRun);
////            String emailId =ConfigReader.getConfigReader().getEmail(userToRun);
////            String otpSecretKey = ConfigReader.getConfigReader().getSecretKey(userToRun);
//
//            if (clickButton()) {
//                try {
//                    // First check if the emailld text is visible (login with saved credentials)
//                    WebElement emailTextElement = context.getCustomActions().getElementWithText(emailId);
//                    if (emailTextElement != null && context.getWebdriverWait()
//                            .until(ExpectedConditions.visibilityOf(emailTextElement)) != null) {
//                        String[] emailIDAndDomain = emailId.split("@");
//                        context.getCustomActions().clickElementWithText(
//                                emailIDAndDomain[0].toUpperCase() + "@" + emailIDAndDomain[1],
//                                "Login with saved credentials button");
//                        logger.info("Clicked on saved credentials button.");
//                        return;
//
//                    }
//                } catch (TimeoutException | NoSuchElementException e) {
//                    // If email input field is visible, proceed to enter email and password
//                    if (context.getWebdriverWait().until(ExpectedConditions.visibilityOf(emailInput)) != null && !context.getDriver().getCurrentUrl().toLowerCase().contains("etool")) {
//
//                        boolean isEmailEntered = inputUserName(emailId);
//                        boolean isPasswordEntered = inputPassword(password);
//                        boolean isOtpEntered = inputOtp(otpSecretKey);
//                        if (isEmailEntered && isPasswordEntered && isOtpEntered) {
//                            logger.info("Logged into application successfully.");
//                            return;
//                        } else {
//                            logger.error("One or more input steps failed. Login process could not be completed.");
//                            throw new RuntimeException("Login process failed due to input errors.");
//                        }
//                    }
//                }
//
//
//            } else {
//                logger.error("Failed to click login button.");
//                throw new RuntimeException("Failed to click on  login button.");
//            }
//
//        } catch (NullPointerException e) {
//            logger.error("A null value for one of the input fields was encountered during the Login process. {}", e.getMessage());
//            throw new RuntimeException("Login failed due to missing data");
//        } catch (Exception e) {
//            logger.error("An unexpected error occurred during login: {}", e.getMessage());
//            throw new RuntimeException("Login failed due to unexpected error. Cause: " + e.getMessage());
//
//        }
//    }


    public void testUserLogin(String emailId, String password, String otp) {
        try {
            context.getCustomActions().customSendKeys(testUserEmailInput, "Email Input Field", emailId.trim());
            context.getCustomActions().customClick(testUserContinueButtonForUserID, "Continue Button");
            context.getCustomActions().customSendKeys(testUserPasswordInput, "Password Input Field", password.trim());
            context.getCustomActions().customClick(testUserContinueButtonForpassword, "Continue Button");
            context.getCustomActions().customClick(authenticatorSelection, "Continue Button for Authentication Selection");
            enterOtpTestUser(otp);
            context.getCustomActions().customClick(testUserLoginButton, "Test User Login Button");

        } catch (Exception e) {
            logger.error("Error occured during test user sign in. Cause {}", e.getMessage());
        }
    }


    public void enterOtpTestUser(String otp) {
        context.getCustomWait().waitForElementPresent(By.cssSelector("div.otc"), "Fields to enter OTP");
        int inputIdCounter = 1;
        for (int i = 0; i < otp.length(); i++) {
            By inputLocator = By.id("otc-" + String.valueOf(inputIdCounter));
            context.getCustomActions().customSendKeys(inputLocator, "OTP" + Integer.toString(inputIdCounter),
                    String.valueOf(otp.charAt(i)));
            inputIdCounter++;

        }


    }
}

