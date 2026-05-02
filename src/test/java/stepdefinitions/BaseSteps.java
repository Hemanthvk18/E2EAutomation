package stepdefinitions;

import data.NormalizedRow;
import data.ScenarioDataProvider;
import managers.TestContextManager;

public abstract class BaseSteps {

    protected final TestContextManager context;
    protected final ScenarioDataProvider dataProvider;

    protected BaseSteps(TestContextManager context) {
        this.context = context;
        this.dataProvider = context.data();
    }
    protected String moduleName() {
        return context.getCurrentModule();
    }

    protected NormalizedRow first(String sheetName) {
        return dataProvider.getFirstRow(moduleName(), sheetName);
    }
}
