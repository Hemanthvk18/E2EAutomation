package utilities;

import io.qameta.allure.Allure;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.Collator;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class CustomStringUtils {

    private static final Logger logger = LoggerFactory.getLogger(CustomStringUtils.class);

    public static boolean checkForDuplicates(List<String> list) {
        Set<String> seenElements = new HashSet<>();
        // Iterate over the list and check if the element is already in the set
        for (String element : list) {
            if (!seenElements.add(element)) {
                return false;
            }
        }
        return true;
    }

    public static boolean compareListOfStringElements(List<String> expected, List<String> actual, String elementType, String elementName, boolean compareOrder) {
        if (compareOrder) {
            return compareOrderedLists(expected, actual, elementType, elementName);
        } else {
            return compareUnorderedLists(expected, actual, elementType, elementName);
        }
    }

    private static boolean compareOrderedLists(List<String> expected, List<String> actual,
                                               String elementType, String elementName) {

        boolean compareStatus = true;
        logger.info("Starting Ordered list comparison for {} {} values.", elementName, elementType);
        for (int i = 0; i < Math.min(expected.size(), actual.size()); i++) {
            if (!expected.get(i).trim().equals(actual.get(i).trim())) {
                logger.error("Mismatch at index {}: Expected- {}, Actual- {}", i, expected.get(i), actual.get(i));

                Allure.addAttachment("Mismatch Values",
                        "Mismatch at index " + i + ": Expected - " + expected.get(i) + " Actual " + actual.get(i));
                compareStatus = false;

            }
        }
        // Handle remaining elements (size mismatch)

        if (expected.size() > actual.size()) {
            List<String> missing = expected.subList(actual.size(), expected.size());
            missing.forEach(value -> {
                int expectedIndex = expected.indexOf(value);
                int actualIndex = actual.indexOf(value);

                logger.error("Missing value at index {): '{}'", expectedIndex, value);
                Allure.addAttachment("Missing value at index " + actualIndex, value);
            });
            compareStatus = false;

        } else if (actual.size() > expected.size()) {
            List<String> extra = actual.subList(expected.size(), actual.size());
            extra.forEach(value -> {
                int index = actual.indexOf(value);
                logger.error("Extra value at index {): '{}'", index, value);
                Allure.addAttachment("Extra value at index " + index, value);
            });

            compareStatus = false;
        }
        if (compareStatus) {
            logger.info("Actual values MATCH expected values in {} {} values.", elementName, elementType);
        } else {
            logger.error("Actual values DO NOT MATCH expected values in {} {} values.", elementName, elementType);
            throw new AssertionError(
                    "Actual values DO NOT MATCH expected values in " + elementName + " " + elementType + "values.");
        }
        return compareStatus;
    }

    private static boolean compareUnorderedLists(List<String> expected, List<String> actual,
                                                 String elementType, String elementName) {
        boolean compareStatus = true;

        logger.info("Starting Unordered list comparison for {} {} values.", elementName, elementType);
        Map<String, Integer> expectedCount = getElementCounts(expected);
        Map<String, Integer> actualCount = getElementCounts(actual);

        for (String key : expectedCount.keySet()) {
            int expectedOccurrences = expectedCount.get(key);
            int actualOccurrences = actualCount.getOrDefault(key, 0);
            if (expectedOccurrences > actualOccurrences) {

                logger.error("Missing value in actual list for '{}': Expected {}, Found {}", key, expectedOccurrences, actualOccurrences);
                Allure.addAttachment("Missing values:", "Missing value in actual list for '" + key + "': Expected -"
                        + expectedOccurrences + ", Found - " + actualOccurrences);
                compareStatus = false;
            }
        }

        for (String key : actualCount.keySet()) {
            int actualOccurrences = actualCount.get(key);
            int expectedOccurrences = expectedCount.getOrDefault(key, 0);

            if (actualOccurrences > expectedOccurrences) {
                logger.error("Extra value in actual list for '{}': Found {}, Expected {}", key, actualOccurrences,
                        expectedOccurrences);
                Allure.addAttachment("Extra values:", "Extra value actual list for'" + key + " ': Found - "
                        + actualOccurrences + ", Expected -" + expectedOccurrences);
                compareStatus = false;
            }
        }

        if (compareStatus) {
            logger.info("Actual values MATCH expected values in {} {} values.", elementName, elementType);
        } else {
            logger.error("Actual values DO NOT MATCH expected values in {} {} values.", elementName, elementType);
            throw new AssertionError(
                    "Actual values DO NOT MATCH expected values in" + elementName + " " + elementType + " values.");
        }
        return compareStatus;

    }

    private static Map<String, Integer> getElementCounts(List<String> list) {
        return list.stream().
                map(String::trim).
                collect(Collectors.toMap(value -> value, value -> 1, Integer::sum));
    }

    public static String getCurrentDateAsString(String pattern) {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        String formattedDate = currentDate.format(formatter);
        return formattedDate;

    }

    public static String getDatePlusDaysAsString(String pattern, int plusNumberOfDays) {
        LocalDate currentDate = LocalDate.now();
        LocalDate newDate = currentDate.plusDays(plusNumberOfDays);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return newDate.format(formatter);
    }

    public static String normalizeDateForInput(String rawDate) {
        try {
            DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("[d-M-yyyy][d/m/yyyy]");
            LocalDate parsed = LocalDate.parse(rawDate.trim(), inputFormat);
            return parsed.format(DateTimeFormatter.ofPattern("d M yyyy"));
        } catch (DateTimeParseException e) {
            logger.error("Invalid date format: {}", rawDate);
            throw new IllegalArgumentException("Invalid date format: " + rawDate, e);
        }
    }


    public static boolean isValuesSortedAscending(List<String> values) {
        List<String> sortedList = new ArrayList<>(values);
        Collections.sort(sortedList);
        return values.equals(sortedList);

    }

    public static boolean isValuesSortedDescending(List<String> values) {

        List<String> sortedList = new ArrayList<>(values);
        Collections.sort(sortedList, Collections.reverseOrder());
        return values.equals(sortedList);
    }

    public static boolean verifySorted(List<String> originalValues, List<String> sortedValues, boolean ascendingOrder) {

        if (originalValues == null || sortedValues == null) {
            return false; // Either list is null, sorting is not valid
        }

        if (originalValues.isEmpty() && sortedValues.isEmpty()) {
            return true; // Both lists are empty, they are hey are trivially sorted }
        }

        // Sort the original list in the desired order (ascending or descending)
        List<String> sortedOriginalList = new ArrayList<>(originalValues);

        // Custom comparator to handle null values
        Comparator<String> comparator = ascendingOrder ? Comparator.nullsFirst(Comparator.naturalOrder()) // Nulls first
                // for
                // ascending
                // order
                : Comparator.nullsLast(Comparator.reverseOrder()); // Nulls last for descending order

        // Sort using the comparator
        sortedOriginalList.sort(comparator);

        logger.info("Expected Sorted List:{}", sortedOriginalList);
        logger.info("Actual Sorted List: {}", sortedValues);

        // Compare the sorted original list with the sorted list
        return sortedOriginalList.equals(sortedValues);
    }

    public static boolean isListSorted(List<String> list, boolean ascending, boolean numericSort) {
        if (list.size() <= 1)
            return true;

        for (int i = 0; i < list.size() - 1; i++) {
            String current = list.get(i);
            String next = list.get(i + 1);

            int comparison;
            if (numericSort) {

                // Handle empty strings for numeric parsing

                double num1 = StringUtils.isBlank(current) ? Double.MIN_VALUE
                        : Double.parseDouble(current.replaceAll("[^\\d.]", ""));
                double num2 = StringUtils.isBlank(next) ? Double.MIN_VALUE
                        : Double.parseDouble(next.replaceAll("[^\\d.]", ""));
                comparison = Double.compare(num1, num2);

            } else {
                Collator collator = Collator.getInstance(Locale.US);
                collator.setStrength(Collator.SECONDARY);
                comparison = collator.compare(current, next);
            }
            if (ascending && comparison > 0)
                return false;

            if (!ascending && comparison < 0)
                return false;
        }
        return true;
    }

    public static boolean isListSorted(List<String> list, boolean ascending) {
        if (list.size() <= 1)
            return true;
        Collator collator = Collator.getInstance(Locale.US); // Use appropriate locale
        collator.setStrength(Collator.SECONDARY); // Case-insensitive but accent-sensitive

        for (int i = 0; i < list.size() - 1; i++) {
            String current = list.get(i);
            String next = list.get(i + 1);

            int comparison = collator.compare(current, next);

            if (ascending && comparison > 0)
                return false;

            if (!ascending && comparison <= 0)
                return false;
        }

        return true;
    }

    public static List<String> splitToList(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        return Arrays.stream(raw.split("[,;]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    public static String generateRandomFourDigitString() {
        Random random = new Random();
        int number = 1000 + random.nextInt(9000); // ensures 4 digits
        return String.valueOf(number);
    }

    public static String normalizeKey(String s) {
        if (s == null) return null;
        return s.replace('\u00A0', ' ').trim().toLowerCase();
    }

    public static String normalizeValue(String s) {
        if (s == null) return null;
        return s.replace('\u00A0', ' ').trim();

    }

    public static List<String> splitCsvNonEmpty(String raw) {
        if (raw == null) return java.util.Collections.emptyList();
        java.util.regex.Pattern SPLIT = java.util.regex.Pattern.compile("\\s*, \\s*");
        return SPLIT.splitAsStream(normalizeValue(raw))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toList());
    }


    private Map<String, String> parseAttributes(String raw) {
        Map<String, String> map = new LinkedHashMap<>();
        if (raw == null || raw.trim().isEmpty()) return map;
        String[] pairs = raw.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            String k = kv[0].trim();
            String v = (kv.length > 1) ? kv[1].trim() : "";
            if (!k.isEmpty()) map.put(k, v);
        }
        return map;
    }
}





