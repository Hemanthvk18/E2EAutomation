package managers;

import pages.*;

public class PageObjectManager {
    TestContextManager context;
    private LoginPage loginPage;
    private HomePage homePage;
//    private OverviewPage overviewPage;

    public PageObjectManager (TestContextManager context) {
        this.context = context;
    }

    public LoginPage getLoginPage() {
        return (loginPage == null)? loginPage =new LoginPage(context): loginPage;
    }

    public HomePage getHomePage() {
        return (homePage == null)? homePage = new HomePage(context): homePage;
    }


}





