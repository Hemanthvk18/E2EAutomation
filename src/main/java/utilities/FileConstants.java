package utilities;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileConstants {

    private static final Path PROJECT_ROOT = Paths.get(System.getProperty("user.dir"));

    public static final String DOWNLOAD_DIRECTORY = PROJECT_ROOT
            .resolve(Paths.get("src", "test", "resources", "downloads"))
            .toString() + File.separator;

    public static final String DVPR_EXPECTED_COLUMNS_EXCEL_PATH = PROJECT_ROOT
            .resolve(Paths.get("src", "test", "resources", "data-validation", "DVPRExpectedColumnData.xlsx"))
            .toString();

    public static final String INPUT_DATA_DEV = PROJECT_ROOT
            .resolve(Paths.get("src", "test", "resources", "test-data", "dev"))
            .toString() + File.separator;

    public static final String INPUT_DATA_INT = PROJECT_ROOT
            .resolve(Paths.get("src", "test", "resources", "test-data", "int"))
            .toString() + File.separator;

    public static final String INPUT_DATA_STG = PROJECT_ROOT
            .resolve(Paths.get("src", "test", "resources", "test-data", "stg"))
            .toString() + File.separator;

    public static final String COLNAME_COLID_MAP = PROJECT_ROOT
            .resolve(Paths.get("src", "test", "resources", "test-data", "ColNameToColIdMap.xlsx"))
            .toString();

    public static final String AGGRID_EXPECTED_COLUMN_NAMES = PROJECT_ROOT
            .resolve(Paths.get("src", "test", "resources", "data-validation", "AGGridExpectedColumnNames.xlsx"))
            .toString();
}