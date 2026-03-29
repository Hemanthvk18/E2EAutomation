package utilities;


import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.ColumnMeta;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class ExcelUtility {
    private static final Logger logger = LoggerFactory.getLogger(ExcelUtility.class);

    public static Workbook openWorkbook(String filePath) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(new File(filePath))) {
            return getWorkbook(fileInputStream, filePath);
        }
    }

    public static void closeWorkbook(Workbook workbook) {
        try {
            workbook.close();
        } catch (Exception e) {
            logger.info("Error closing workbook.");
        }
    }

    public static Map<String, List<Map<String, String>>> readInputDataFromExcelAllSheets(String excelFilePath, String filterColumn, String filterValue, int startRow, int sheetStartIndex) throws IOException {

        Map<String, List<Map<String, String>>> allSheetData = new LinkedHashMap<>();

        try (Workbook workbook = openWorkbook(excelFilePath)) {
            for (int sheetIndex = sheetStartIndex; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                String sheetName = sheet.getSheetName();

                if (sheetName == null || sheetName.trim().isEmpty()) {
                    continue;
                }

                Map<String, Integer> columnIndexMap = getColumnIndexMap(sheet, startRow);
                List<Map<String, String>> sheetDataList = new ArrayList<>();
                for (int rowIndex = startRow; rowIndex < sheet.getPhysicalNumberOfRows(); rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row != null) {
                        boolean shouldSkipRow = false;
                        if (filterColumn != null && filterValue != null) {
                            String cellValue = getCellValue(row, columnIndexMap.get(filterColumn));
                            if (cellValue == null || !cellValue.equalsIgnoreCase(filterValue)) {
                                shouldSkipRow = true;
                            }
                        }
                        if (!shouldSkipRow) {
                            Map<String, String> rowData = extractRowData(row, columnIndexMap);
                            sheetDataList.add(rowData); // Add row data to the sheetDataList }
                        }
                    }

                }
                if (!sheetDataList.isEmpty()) {
                    allSheetData.put(sheetName, sheetDataList); // Put the list of rows for the sheet in the map
                }
            }
            closeWorkbook(workbook);
        } catch (IOException e) {
            logger.error("Error reading Excel file at " + excelFilePath, e.getMessage());
            throw e;

        }

        return allSheetData;
    }

    public static List<Map<String, String>> readInputDataFromExcelSheetWithFilter(String excelFilePath,
                                                                                  String sheetNameInput,
                                                                                  String filterColumnName, String filterColumnValue, Integer headerRowStartIndex,
                                                                                  String columnNameOfIndexColumn) throws IOException {
        List<Map<String, String>> filteredSheetData = new ArrayList();
        if (filterColumnName == null || filterColumnValue == null || columnNameOfIndexColumn == null) {
            throw new IllegalArgumentException("Filter Column, Filter Value and Index Column Name cannot be null.");

        }
        logger.info("Reading data from sheet {} in excel{} with filter {}={} that has the {} as the {} of {}.",
                sheetNameInput, excelFilePath, filterColumnName, filterColumnValue, columnNameOfIndexColumn, columnNameOfIndexColumn, filterColumnName);

        try (Workbook workbook = openWorkbook(excelFilePath)) {
            if (sheetNameInput == null || sheetNameInput.trim().isEmpty()) {
                throw new IllegalArgumentException("Sheet name cannot be null.");
            }
            Sheet sheet = workbook.getSheet(sheetNameInput);
            Map<String, Integer> headerColumnIndexMap = getColumnIndexMap(sheet, headerRowStartIndex);
            // check if the filterColumn exists
            if (!headerColumnIndexMap.containsKey(filterColumnName.trim())
                    || headerColumnIndexMap.containsKey(columnNameOfIndexColumn)) {
//            throw new IllegalArgumentException("Filter Column" + filterColumnName+"' or " +"Index Column" + filterColumnName + "does not exist in the sheet.");
                logger.warn("Filter Column '" + filterColumnName + "' or" + "Index Column '" + filterColumnName +
                        "'" + "does not exist in the sheet.");
            }
            Integer filterColumnIndex = headerColumnIndexMap.get(filterColumnName.trim());
            Integer indexColumnIndex = headerColumnIndexMap.get(columnNameOfIndexColumn);
            String matchingIndexCellValue = null;
            boolean filterValueFound = false;
            boolean collectRows = false; // Find the index of filter value

            for (int rowIndex = headerRowStartIndex + 1; rowIndex < sheet.getPhysicalNumberOfRows(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    logger.info("Skipping empty row at Index:{} in Sheet:{}.", rowIndex, sheetNameInput);
                    continue;
                }


                String currentFilterColumnCellValue = getCellValue(row, filterColumnIndex);
                String currentIndexCellValue = getCellValue(row, indexColumnIndex);

                // Step 1 find the matching index
                if (matchingIndexCellValue == null && currentFilterColumnCellValue != null
                        && currentFilterColumnCellValue.equalsIgnoreCase(filterColumnValue)) {
                    matchingIndexCellValue = currentIndexCellValue;
                    filterValueFound = true;
                    collectRows = true;    // start collecting rows

                }

                // Step 2 collect rows with matching index

                if (collectRows && matchingIndexCellValue != null && currentIndexCellValue != null
                        && currentIndexCellValue.equalsIgnoreCase(matchingIndexCellValue)) {
                    Map<String, String> rowDataMap = new LinkedHashMap<String, String>();
                    rowDataMap = extractRowData(row, headerColumnIndexMap);
                    filteredSheetData.add(rowDataMap);
                }


                if (collectRows && matchingIndexCellValue != null && currentIndexCellValue != null
                        && currentIndexCellValue.equalsIgnoreCase(matchingIndexCellValue)) {
                    break;// exit loop once rows with matching index are done. this is done under the
                    // assumption that all the indexes for that filter column are grouped together
                }
            }
            if (!filterValueFound) {
                throw new NoSuchElementException(
                        "No data found for filter:" + filterColumnName + " with value:" + filterColumnValue + ".");
            }
            if (matchingIndexCellValue == null) {
                throw new NoSuchElementException("No index value specified in:" + columnNameOfIndexColumn + " for " + filterColumnName + " with value " + filterColumnValue + ".");
            }
            closeWorkbook(workbook);
            logger.info(
                    "Closing workbook after reading data from sheet {} in excel {} with filter {}={} that has the {} as the {} of {}.",
                    sheetNameInput, excelFilePath, filterColumnName, filterColumnValue, columnNameOfIndexColumn,
                    columnNameOfIndexColumn, filterColumnName);
        } catch (Exception e) {
            logger.error("Error reading Excel file at {} " + excelFilePath, e.getMessage());
            throw e;
//            StackTraceElement elemente=e.getStackTrace()[0];
//            throw new RuntimeException(
//                    "Error occured while reading from excel file:" + excelFilePath + "Filter Column:"
//                    +filterColumnName + ", Filter Value:" + filterColumnValue+ "at" + element.getClassName()
//                            + "."+element.getMethodName() + "(Line:" + element.getLineNumber() + ")",
//                    e);

        }
        return filteredSheetData;
    }

    private static Workbook getWorkbook(FileInputStream fis, String excelFilePath) throws IOException {
        if (excelFilePath.endsWith(".xlsx")) {
            return new XSSFWorkbook(fis);
        } else if (excelFilePath.endsWith(".xls")) {
            return new HSSFWorkbook(fis);
        } else {
            throw new IOException("Unsupported file format: " + excelFilePath);
        }

    }


    private static Map<String, Integer> getColumnIndexMap(Sheet sheet, int startRow) {
        Map<String, Integer> columnIndexMap = new LinkedHashMap<>();
        Row headerRow = sheet.getRow(startRow);

        for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
            String columnName = headerRow.getCell(i).getStringCellValue().trim();
            columnIndexMap.put(columnName, i);
        }
        return columnIndexMap;
    }


    private static Map<String, String> extractRowData(Row row, Map<String, Integer> columnIndexMap) {
        Map<String, String> rowData = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : columnIndexMap.entrySet()) {
            String columnName = entry.getKey();
            Integer columnIndex = entry.getValue();
            String cellValue = getCellValue(row, columnIndex);
            if (cellValue != null && (cellValue.isEmpty())) {
                rowData.put(columnName, cellValue.trim());
            }

        }
        return rowData;
    }


    private static String getCellValue(Row row, Integer columnIndex) {
        if (row == null || columnIndex == null) {
            return null;
        }

        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                double numericValue = cell.getNumericCellValue();
                // If the value is a whole number (e.g., 100, 4), return as an integer
                if (numericValue == (long) numericValue) {
                    return String.valueOf((long) numericValue); // Return as integer (no decimals)
                } else {
                    return String.valueOf(numericValue); // Return as double (with decimals)
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    public static Map<String, String> readRowMap(String excelFilePath, String sheetName) throws IOException {
        Map<String, String> dataMap = new HashMap<>();
        try (Workbook workbook = openWorkbook(excelFilePath)) {
            Sheet sheet = workbook.getSheet(sheetName);
            for (Row row : sheet) {
                String header = getCellValue(row, 0);
                String valueString = getCellValue(row, 1);
                dataMap.put(header, valueString);
            }
        }
        return dataMap;
    }


    public static Map<String, ColumnMeta> readColumnMeta(String excelFilePath, String sheetName) throws IOException {
        Map<String, ColumnMeta> columnMetaMap = new HashMap<>();
        try (Workbook workbook = openWorkbook(excelFilePath)) {
            Sheet sheet = workbook.getSheet(sheetName);
            for (Row row : sheet) {
                String colName = getCellValue(row, 0);
                String colId = getCellValue(row, 1);
                String elementType = getCellValue(row, 2);
                String editableFlag = getCellValue(row, 3);
                if (colName != null && colId != null && elementType != null) {
                    boolean editable = editableFlag != null && editableFlag.equalsIgnoreCase("yes");
                    columnMetaMap.put(colName.trim(), new ColumnMeta(colId.trim(), elementType.trim(), editable));

                }

            }
        }
        return columnMetaMap;
    }

    public static Map<String, Integer> readColumnNamesFromExcel(String excelFilePath, String sheetName) throws IOException {

        Map<String, Integer> columnNamesData = new LinkedHashMap<String, Integer>();
        try (Workbook workbook = openWorkbook(excelFilePath)) {
            columnNamesData = getColumnIndexMap(workbook.getSheet(sheetName), 0);
        }
        return columnNamesData;
    }

    public static List<String> readColumnData(String excelFilePath, String sheetName, String columnName)
            throws IOException {
        List<String> columnDataList = new ArrayList<String>();
        try (Workbook workbook = openWorkbook(excelFilePath)) {
            Sheet sheet = workbook.getSheet(sheetName);
            Map<String, Integer> columnIndexMap = getColumnIndexMap(sheet, 0);
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Cell cell = row.getCell(columnIndexMap.get(columnName));
                    if (cell != null) {
                        String cellValue = cell.toString().trim();
                        if (!(cellValue.isBlank()) && (!cellValue.isEmpty()) && cellValue != null) {
                            columnDataList.add(cell.toString());
                        }
                    }
                }
            }
            return columnDataList;
        }
    }

    public static String getSheetNameByIndex(String excelFilePath, int sheetIndex) throws IOException {
        String sheetName = null;
        try (Workbook workbook = openWorkbook(excelFilePath)) {
            sheetName = workbook.getSheetName(sheetIndex);
        } catch (IllegalArgumentException e) {
            logger.error("Specified sheet '" + sheetName + "'" + " not present at index:" + sheetIndex +
                    "in workbook for file:" + excelFilePath);
        }
        return sheetName;
    }


    public static List<Map<String, String>> readExportFile(Path file) {
        return readExportFile(file, null);
    }

    public static List<Map<String, String>> readExportFile(Path file, String sheetNameOrNull) {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        if (name.endsWith(".xlsx")) {
            return readXlsxToMaps(file, sheetNameOrNull);
        } else if (name.endsWith(".xls")) {
            // If you ever export legacy xls; otherwise you may remove this branch.
            return readXlsxToMaps(file, sheetNameOrNull);
        }
        throw new IllegalArgumentException("Unsupported export type: " + file);
    }

    private static List<Map<String, String>> readXlsxToMaps(Path file, String sheetNameOrNull) {
        List<Map<String, String>> out = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file.toFile());
             Workbook wb = WorkbookFactory.create(fis)) {
            Sheet sheet = (sheetNameOrNull == null || sheetNameOrNull.isBlank())
                    ? wb.getSheetAt(0)
                    : wb.getSheet(sheetNameOrNull);
            if (sheet == null) throw new IllegalArgumentException("Sheet not found: " + sheetNameOrNull);
            int firstRow = sheet.getFirstRowNum();
            Row headerRow = sheet.getRow(firstRow);
            if (headerRow == null) throw new IllegalStateException("Header row missing at row" + firstRow);

            List<String> headers = new ArrayList<>();
            for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                headers.add(normalize(cellToString(headerRow.getCell(c))));
            }

            for (int r = firstRow + 1; r < sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                Map<String, String> m = new LinkedHashMap<>();
                for (int c = 0; c < headers.size(); c++) {
                    String key = headers.get(c);
                    if (key == null || key.isEmpty()) continue;
                    m.put(key, normalize(cellToString(row.getCell(c))));
                }
                if (m.values().stream().anyMatch(v -> v != null && !v.isEmpty())) out.add(m);
            }
            return out;

        } catch (Exception e) {
            logger.error("Error reading .xlsx export: {}", file, e);
            throw new RuntimeException("Failed to read Excel export: " + file + " - " + e.getMessage(), e);

        }
    }

    private static List<Map<String, String>> readXlsToMaps(Path file, String sheetNameOrNull) {
// This is identical to readXLsxToMaps, but kept separate for clarity.
        return readXlsxToMaps(file, sheetNameOrNull);
    }

    private static String cellToString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
// ISO-8601 date (yyyy-MM-dd); adjust if you need time too
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                } else {
// preserve formatting (no scientific notation)
                    return NumberToTextConverter.toText(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case FORMULA:
// Try text; if numeric formula, fall back to numeric-as-text
                try {
                    return cell.getStringCellValue();
                } catch (IllegalStateException e) {
                    try {
                        return NumberToTextConverter.toText(cell.getNumericCellValue());
                    } catch (IllegalStateException ex) {
                        return "";
                    }
                }
            default:
                return "";
        }
    }

    private static String normalize(String v) {
        if (v == null) return "";
        // Convert NBSP to space and trim
        return v.replace('\u00A0', ' ').trim();
    }

    public static List<Map<String, String>> readRowsByFilterAndIndex(
            String excelFilePath,
            String sheetName,
            String filterColumnName,
            String filterColumnValue,
            String indexColumnName, String indexValue,
            int headerRowsStartIndex) throws IOException {

        Objects.requireNonNull(excelFilePath, "excelFilePath");
        Objects.requireNonNull(sheetName, "sheetName");
        Objects.requireNonNull(filterColumnName, "filterColumnName");
        Objects.requireNonNull(indexColumnName, "indexColumnName");

        List<Map<String, String>> rows = new ArrayList<>();
        try (Workbook workbook = openWorkbook(excelFilePath)) {
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet not found: " + sheetName);
            }

            // Build header map: HeaderText> ColumnIndex Row headerRow sheet.getRow(headerRowStartIndex);
            Row headerRow = sheet.getRow(headerRowsStartIndex);
            if (headerRow == null) {
                throw new IllegalStateException("Header row not found at index " + headerRowsStartIndex);
            }

            Map<String, Integer> headerIndex = new LinkedHashMap<>();
            for (int c = 0; c < headerRow.getPhysicalNumberOfCells(); c++) {
                Cell hc = headerRow.getCell(c);
                if (hc == null) continue;
                if (hc.getCellType() == CellType.STRING) {
                    String name = normalize(hc.getStringCellValue());
                    if (!name.isEmpty()) headerIndex.put(name, c);
                } else {
                    String name = normalize(hc.toString());
                    if (!name.isEmpty()) headerIndex.put(name, c);
                }
            }

            Integer filterIdx = headerIndex.get(filterColumnName);
            Integer indexIdx = headerIndex.get(indexColumnName);

            if (filterIdx == null) {
                throw new IllegalArgumentException("Filter column not found in header: " + filterColumnName);
            }

            if (indexIdx == null) {
                throw new IllegalArgumentException("Index column not found in header: " + indexColumnName);
            }


// Iterate data rows

            int firstDataRow = headerRowsStartIndex + 1;
            int lastRowSheet = sheet.getPhysicalNumberOfRows() - 1;

            for (int r = firstDataRow; r <= lastRowSheet; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                String filterVal = normalize(getCellValue(row, filterIdx));
                String idxVal = normalize(getCellValue(row, indexIdx));

                if (filterVal.equalsIgnoreCase(normalize(filterColumnValue))
                        && idxVal.equalsIgnoreCase(normalize(indexValue))) {

                    Map<String, String> rowMap = new LinkedHashMap<>();
                    for (Map.Entry<String, Integer> e : headerIndex.entrySet()) {
                        String key = e.getKey();
                        Integer colIdx = e.getValue();
                        String val = normalize(getCellValue(row, colIdx));
                        // Keep empty strings as values to preserve column presence (helps comparison)
                        rowMap.put(key, val);
                    }
                    // If the entire row is blank, skip it
                    boolean anyNonBlank = rowMap.values().stream().anyMatch(v -> v != null && !v.isEmpty());
                    if (anyNonBlank) rows.add(rowMap);
                }
            }
        }

        if (rows.isEmpty()) {
            throw new NoSuchElementException(
                    "No rows found for " + filterColumnName + "=" + filterColumnValue + " and " + indexColumnName + "=" + indexValue + "in sheet" + sheetName + "'");
        }
        return rows;
    }

    public static List<Map<String, String>> readRowsByFilterAndIndex(
            String excelFilePath,
            String sheetName,
            String filterColumnName, String filterColumnValue,
            String indexColumnName, String indexValue) throws IOException {
        return readRowsByFilterAndIndex(
                excelFilePath, sheetName, filterColumnName, filterColumnValue, indexColumnName, indexValue, 0);

    }
}





