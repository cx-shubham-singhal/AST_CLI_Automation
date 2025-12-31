package com.myorg.cxone.helpers;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileInputStream;
import java.util.*;

public class ExcelDataProvider {

    // 🔹 Generic version (works with dynamic headers)
    public static Object[][] getExcelDataAsMap(String filePath, String sheetName) {
        List<Map<String, String>> data = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            Row headerRow = sheet.getRow(0);
            int colCount = headerRow.getLastCellNum();

            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, String> baseMap = new HashMap<>();

                for (int j = 0; j < colCount; j++) {
                    String key = headerRow.getCell(j).getStringCellValue();
                    Cell cell = row.getCell(j);
                    String value = cell != null ? cell.toString().trim() : "";
                    baseMap.put(key, value);
                }

                String additionalFlags = baseMap.get("AdditionalFlags");
                List<String> expandedFlags = expandAdditionalFlags(additionalFlags);

                // 🔹 duplicate row per expanded flag
                for (String flag : expandedFlags) {
                    Map<String, String> cloned = new HashMap<>(baseMap);
                    cloned.put("AdditionalFlags", flag);
                    data.add(cloned);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Object[][] result = new Object[data.size()][1];
        for (int i = 0; i < data.size(); i++) {
            result[i][0] = data.get(i);
        }
        return result;
    }

    private static List<String> expandAdditionalFlags(String flags) {
        List<String> expanded = new ArrayList<>();

        if (flags == null || flags.trim().isEmpty()) {
            expanded.add("");
            return expanded;
        }

        flags = flags.trim();

        if (flags.startsWith("`")) {
            flags = flags.substring(1).trim();
        }

        flags = normalizeFlagPrefix(flags);

        // Case: flags with '=' syntax → run once
        if (flags.contains("=")) {
            expanded.add(flags);
            return expanded;
        }

        // Case: no-value boolean flags → run once
        if (!flags.contains("\"") && flags.split("\\s+").length == 1) {
            expanded.add(flags);
            return expanded;
        }

        // Case: space-separated flag with quoted value
        if (flags.matches("^--\\S+\\s+\".*\"$")) {

            String quotedValue =
                    flags.replaceAll("^--\\S+\\s+\"(.*)\"$", "$1");

            // 🔹 NEW: empty value ("") → quotes mandatory → run once
            if (quotedValue.isEmpty()) {
                expanded.add(flags);
                return expanded;
            }

            // Existing rule: value contains spaces → run once
            if (quotedValue.contains(" ")) {
                expanded.add(flags);
                return expanded;
            }

            // Existing rule: non-empty, no spaces → run twice
            expanded.add(flags);

            String withoutQuotes = flags.replace("\"", "");
            withoutQuotes = normalizeFlagPrefix(withoutQuotes);

            expanded.add(withoutQuotes);
            return expanded;
        }

        expanded.add(flags);
        return expanded;
    }


    private static String normalizeFlagPrefix(String flag) {
        if (flag == null) {
            return "";
        }

        flag = flag.trim();

        while (flag.startsWith("`")) {
            flag = flag.substring(1).trim();
        }

        if (flag.startsWith("-") && !flag.startsWith("--")) {
            flag = flag.substring(1);
        }

        if (!flag.startsWith("--")) {
            flag = "--" + flag;
        }

        return flag;
    }

}