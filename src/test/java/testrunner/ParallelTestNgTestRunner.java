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
                "html:target/cucumber-report.html",
                "json:target/cucumber-reports/cucumber.json",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
                "rerun:target/rerun.txt"},
        tags = "@Regression")

public class ParallelTestNgTestRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = true) // Set to true for parallel execution, false for sequential
    public Object[][] scenarios() {
        return super.scenarios();
    }   //here cucumber scenarios are passing to testng for execution

    @BeforeSuite
    public void beforeSuite() throws Exception {
        FileUtility.cleanUpFolder("allure-results");
//        FileUtility.cleanUpFolder("target/allure-results");
        System.out.println("Allure results cleaned...");
    }
}



/**
 * ===============================================================
 * Cucumber + TestNG Runner
 * ===============================================================
 *
 * Purpose:
 * --------
 * This class acts as the entry point for Cucumber execution.
 * It connects Cucumber feature files with TestNG.
 *
 * Responsibilities:
 * ----------------
 * 1. Define feature file location.
 * 2. Define glue code (step definitions and hooks).
 * 3. Configure reporting plugins.
 * 4. Configure tag execution.
 * 5. Enable/disable scenario parallel execution through DataProvider.
 *
 * Execution Flow:
 * --------------
 * testng.xml
 *      ↓
 * ParallelTestNgTestRunner
 *      ↓
 * Feature Files
 *      ↓
 * Step Definitions
 *
 * Parallel Execution:
 * ------------------
 * @DataProvider(parallel = true)
 *      → Executes scenarios in parallel.
 *
 * @DataProvider(parallel = false)
 *      → Executes scenarios sequentially.
 *
 * Note:
 * ----
 * This class controls WHAT to execute.
 * testng.xml controls HOW to execute.
 *
 * Common Usage:
 * ------------
 * Local Execution:
 *      Run this class directly from IDE.
 *
 * CI/CD Execution:
 *      Triggered through Maven → testng.xml → Runner.
 *
 * ===============================================================
 */

