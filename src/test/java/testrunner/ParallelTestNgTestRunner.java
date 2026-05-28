package testrunner;

//@RunWith(Cucumber.class)

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import utilities.FileUtility;

@Listeners({org.testng.reporters.FailedReporter.class})
@CucumberOptions(features = "src/test/java/features/",
        glue = {"stepdefinitions", "Hook"},
        monochrome = true,
        plugin = {"pretty",
                "html:target/cucumber-reports/cucumber-pretty",
                "json:target/cucumber-reports/cucumber.json",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
                "rerun:target/rerun.txt"},
        tags = "@Regression")

public class ParallelTestNgTestRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = false) // Set to true for parallel execution, false for sequential
    public Object[][] scenarios() {
        return super.scenarios();
    }

    @BeforeSuite
    public void beforeSuite() throws Exception {
        FileUtility.cleanUpFolder("allure-results");
//        FileUtility.cleanUpFolder("target/allure-results");
        System.out.println("Allure results cleaned...");
    }
}

