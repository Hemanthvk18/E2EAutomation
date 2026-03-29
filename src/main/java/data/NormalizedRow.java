package data;

import utilities.CustomStringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NormalizedRow {
    private final Map<String, String> original;
    private final Map<String, String> normToOriginal;

    public NormalizedRow(Map<String, String> row) {
        this.original = Objects.requireNonNull(row);
        this.normToOriginal = new HashMap<>();
        for (String k : row.keySet()) {
            if (k != null) normToOriginal.put(CustomStringUtils.normalizeKey(k), k);
        }
    }

    public String get(String key) {
        String orig = normToOriginal.get(CustomStringUtils.normalizeKey(key));
        String v = (orig == null) ? null : original.get(orig);
        return v == null ? null : v.replace('\u00A0', ' ').trim();
    }

    public String requireNonBlank(String key, String ctx) {
        String v = get(key);
        if (v == null || v.isEmpty()) {
            throw new IllegalArgumentException("Required column '" + key + "' is missing/blank. " + ctx +
                    ". Available keys:" + original.keySet()
            );
        }
        return v;
    }

    public Map<String, String> original() {
        return original;
    }
}


