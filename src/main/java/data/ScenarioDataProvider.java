package data;


import managers.TestContextManager;
import utilities.ExcelUtility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScenarioDataProvider {
    private final TestContextManager context;
    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ScenarioDataProvider.class);
    // Cache per sheet per scenario
    private final Map<String, List<NormalizedRow>> cacheByKey = new HashMap<>();

    public ScenarioDataProvider(TestContextManager context) {
        this.context = context;
    }

    public List<NormalizedRow> getRows(String moduleName, String sheetName) {
        String key = moduleName + "::" + sheetName;
        List<NormalizedRow> rows = cacheByKey.computeIfAbsent(key, k -> {
            try {
                String excelPath = context.getInputDataExcelPath(moduleName);
                List<Map<String, String>> data =
                        ExcelUtility.readInputDataFromExcelSheetWithFilter(excelPath, sheetName, "Scenario Name", context.getScenarioName(), 0,
                                "Index"
                        );
                if (data == null || data.isEmpty()) {
                    throw new IllegalStateException(
                            "No data rows for module='" + moduleName + "', sheet '" + sheetName +
                                    "', scenario='" + context.getScenarioName() + "', file='" + excelPath + "'.");
                }

                return data.stream().map(NormalizedRow::new).toList();

            } catch (Exception e) {
                // log with context
                throw new RuntimeException(e);
            }
        });
        return java.util.Collections.unmodifiableList(rows);
    }

    public NormalizedRow getFirstRow(String moduleName, String sheetName) {
        return getRows(moduleName, sheetName).get(0);
    }

    public List<Map<String, String>> getOriginalRows(String moduleName, String sheetName) {
        return java.util.Collections.unmodifiableList(
                getRows(moduleName, sheetName).stream().map(NormalizedRow::original).toList()

        );

    }

    public void reset() {
        cacheByKey.clear();
    }
}


