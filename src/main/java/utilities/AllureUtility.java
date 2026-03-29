package utilities;

import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class AllureUtility {

    private static final Logger logger = LoggerFactory.getLogger(AllureUtility.class);

    public static void generateAllureReport(String reportDir,
                                            String allureResultsDir,
                                            String customTitle,
                                            String newIndexFileName,
                                            String browser,
                                            String url) throws IOException, InterruptedException {
        Path resultsPath = Paths.get(allureResultsDir);
        Files.createDirectories(resultsPath);

        // Write environment.properties to the provided allure-results dir
        createEnvironmentProperties(browser, url, System.getProperty("os.name"), resultsPath);
        // Compute a temporary multi-file directory to obtain fresh 'history/'

        Path finalReport = Paths.get(reportDir);
        Path tempMulti = (finalReport.getParent() == null)
                ? Paths.get(reportDir + "-multi")
                : finalReport.getParent().resolve(finalReport.getFileName().toString() + "-multi");

        //PASS 1: multi-file (produces 'history/')
        int pass1 = runAllureGenerate(resultsPath.toString(), tempMulti.toString(), customTitle, false);
        if (pass1 != 0) {
            logger.error("Allure multi-file generation failed (exit code {}). Aborting.", pass1);
            return;

        }

        // -------------nPASS 2:single - file( final artifact)

        int pass2 = runAllureGenerate(resultsPath.toString(), finalReport.toString(), customTitle, true);
        if (pass2 != 0) {
            logger.error("Allure single-file generation failed (exit code {}).", pass2);
            return;
        }

        // Optional: rename index.html -> <newIndexFileName> (no duplicate file)
        if (newIndexFileName != null && !newIndexFileName.trim().isEmpty()) {
            renameIndexFile(finalReport.toString(), newIndexFileName.trim());
        }
        logger.info("Allure single-file report generated at: {}", finalReport.toAbsolutePath());

        // Optional: copy index.html to a custom filename (do not rename index.html)
//        if (newIndexFileName != null && !newIndexFileName.trim().isEmpty()) {
//            try {
//                FileUtility.copyFile(
//                        finalReport.resolve("index.html"),
//                        finalReport.resolve(newIndexFileName)
//                );
//                logger.info("Copied index.html to {}", finalReport.resolve(newIndexFileName).toAbsolutePath());
//            } catch (IOException e) {
//                logger.error("Failed to copy index.html to {}", newIndexFileName, e);
//            }
//        }


        // Persist fresh history for the NEXT run

        try {
            FileUtility.copyDir(
                    tempMulti.resolve("history"),
                    Paths.get(".last-history", "history")
            );
            logger.info("Persisted history to {}", Paths.get(".last-history", "history").toAbsolutePath());

        } catch (IOException e) {
            logger.warn("Could not persist history for next run: {}", e.getMessage());
        }


        // Cleanup temp multi-file folder
        try {
            FileUtility.cleanFolders(tempMulti.toString());

        } catch (IOException e) {
            logger.warn("Failed to clean temp multi-file directory {}: {}", tempMulti.toAbsolutePath(), e.getMessage());
        }

    }


    private static int runAllureGenerate(String allureResultsDir, String outputDir,

                                         String title, boolean singleFile) throws IOException, InterruptedException {

        String baseCmd = singleFile
                ? "allure generate --single-file --clean --report-name \"" + title + "\" \"" + allureResultsDir + "\" -o \"" + outputDir + "\""
                : "allure generate --clean --report-name \"" + title + "\" \"" + allureResultsDir + "\" -o \"" + outputDir + "\"";

        String os = System.getProperty("os.name").toLowerCase();
        ProcessBuilder pb = os.contains("win")
                ? new ProcessBuilder("cmd.exe", "/c", baseCmd)
                : new ProcessBuilder("sh", "-c", baseCmd);
        pb.inheritIO();

        Process p = pb.start();
        int exit = p.waitFor();
        if (exit == 0) {
            logger.info("Allure generate OK -> {}", outputDir);
        } else {
            logger.error("Allure generate FAILED (code {}): {}", exit, baseCmd);
        }
        return exit;
    }

    private static void renameIndexFile(String reportDir, String newIndexFileName) {
        File indexFile = new File(reportDir + "/index.html");
        File renamedFile = new File(reportDir + "/" + newIndexFileName);
        if (indexFile.exists()) {
            boolean success = indexFile.renameTo(renamedFile);
            if (!success) {
                logger.error("Failed to rename index.html to: " + newIndexFileName);
            }
        } else {
            logger.error("index.html not found in " + reportDir);
        }

    }

    /**
     * Writes environment.properties into the given results directory.
     */

    private static void createEnvironmentProperties(String browser, String url, String os, Path resultsDir) {
        try {
            Properties environmentProperties = new Properties();
            environmentProperties.setProperty("BROWSER", browser == null ? "" : browser.toUpperCase());
            environmentProperties.setProperty("URL", url == null ? "" : url);
            environmentProperties.setProperty("OS", os == null ? "" : os.toUpperCase());
            Files.createDirectories(resultsDir);
            try (OutputStream output = Files.newOutputStream(resultsDir.resolve("environment.properties"))) {
                environmentProperties.store(output, "Environment details for Allure Report");
            }

        } catch (Exception e) {
            logger.error("Failed to create environment.properties for Allure report: {}", e.getMessage());
        }
    }

    // ======================== Attachment & step helpers (unchanged) ===================
// Method to generate and attach a table to Allure (plain text tab-separated)
    @Attachment(value = "Table Attachment", type = "text/plain")
    public static String attachTableToAllure(List<Map<String, String>> data) {
        StringBuilder tableBuilder = new StringBuilder();
        if (data != null && !data.isEmpty()) {
            Map<String, String> firstRow = data.get(0);
            // headers
            for (String key : firstRow.keySet()) {
                tableBuilder.append(key).append("\t");
            }
            tableBuilder.append("\n");
            // rows
            for (Map<String, String> row : data) {
                for (String key : firstRow.keySet()) {
                    String value = row.getOrDefault(key, "");
                    tableBuilder.append(value).append("\t");
                }
                tableBuilder.append("\n");
            }
        }
        return tableBuilder.toString();
    }


    public static void addSubStepForData(String tableName, Map<String, String> data, List<String> keysToInclude) {
        Allure.step(tableName, step -> {
            if (data != null && keysToInclude != null) {
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    if (keysToInclude.contains(entry.getKey())) {
                        step.parameter(entry.getKey() + ":", entry.getValue());
                    }
                }
            }
        });
    }

    public static void addSubStepForData(String tableName, String key, String value) {
        Allure.step(tableName, step -> {
            step.parameter(key + ":", value);
        });

    }

    public static void attachExcelFile(String filePath, String attachmentName) {
        File file = new File(filePath);
        try (FileInputStream fis = new FileInputStream(file)) {
            Allure.addAttachment(
                    attachmentName,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    fis,
                    ".xlsx"
            );
        } catch (IOException e) {
            logger.error("Error while attaching Excel file to Allure Report. Cause: {}", e.getMessage(), e);
        }
    }

    public static void captureScreenshot(WebDriver driver, String attachmentName) {
       byte[] screenshot=((TakesScreenshot)driver).getScreenshotAs(org.openqa.selenium.OutputType.BYTES);
        Allure.addAttachment(attachmentName, new ByteArrayInputStream(screenshot));;
    }
}








