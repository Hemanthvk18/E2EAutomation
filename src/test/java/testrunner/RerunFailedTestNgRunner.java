package testrunner;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
        features = "@target/rerun.txt", // <- only failed scenarios from the first run
        glue = {"com.etool.automation.stepdefinitions", "Hook"},
        monochrome = true,
        plugin = {
                "pretty",
                "html:target/cucumber-reports/rerun-pretty",
                "json:target/cucumber-reports/rerun.json",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
                "rerun:target/rerun.txt"
        }
)

public class RerunFailedTestNgRunner extends AbstractTestNGCucumberTests {
    @Override
    @DataProvider(parallel = true) // you can set false if you prefer serial retries
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
