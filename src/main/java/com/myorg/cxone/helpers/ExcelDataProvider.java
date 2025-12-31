package com.myorg.cxone.helpers;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.util.*;

public class ExcelDataProvider {

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
                    Cell headerCell = headerRow.getCell(j);
                    Cell cell = row.getCell(j);

                    String key = headerCell != null ? headerCell.getStringCellValue() : "Column" + j;
                    String value = cell != null ? cell.toString().trim() : "";

                    baseMap.put(key, value);
                }

                String additionalFlags = baseMap.get("AdditionalFlags");
                List<String> expandedFlags = expandAdditionalFlags(additionalFlags);

                // Duplicate test rows as needed
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


    // ==============================
    // FLAG EXPANSION ENGINE
    // ==============================
    private static List<String> expandAdditionalFlags(String flags) {
        List<String> expanded = new ArrayList<>();

        if (flags == null || flags.trim().isEmpty()) {
            expanded.add("");
            return expanded;
        }

        flags = flags.trim();

        // remove leading backtick if present
        if (flags.startsWith("`")) {
            flags = flags.substring(1).trim();
        }

        flags = normalizeFlagPrefix(flags);

        // Case: flags with '=' syntax → run once
        if (flags.contains("=")) {
            expanded.add(flags);
            return expanded;
        }

        // Case: single boolean / no-value flags
        if (!flags.contains("\"") && flags.split("\\s+").length == 1) {
            expanded.add(flags);
            return expanded;
        }

        // Case: flags with quoted value
        if (flags.matches("^--\\S+\\s+\".*\"$")) {

            String quotedValue =
                    flags.replaceAll("^--\\S+\\s+\"(.*)\"$", "$1");

            // CASE: EMPTY VALUE → ONLY ONCE
            // example: --application-name ""
            if (quotedValue.isEmpty()) {
                expanded.add(flags);
                return expanded;
            }

            // CASE: VALUE HAS SPACE → ONLY ONCE
            // example: --application-name "My App Value"
            if (quotedValue.contains(" ")) {
                expanded.add(flags);
                return expanded;
            }

            // CASE: VALUE WITHOUT SPACE → RUN TWICE
            // example: --project-groups "QA_Automation"
            expanded.add(flags); // with quotes

            String withoutQuotes = flags.replace("\"", "");
            withoutQuotes = normalizeFlagPrefix(withoutQuotes);

            expanded.add(withoutQuotes); // without quotes
            return expanded;
        }

        // Default fallback
        expanded.add(flags);
        return expanded;
    }

    private static String normalizeFlagPrefix(String flag) {
        if (flag == null) return "";

        flag = flag.trim();

        // strip accidental repeated backticks
        while (flag.startsWith("`")) {
            flag = flag.substring(1).trim();
        }

        // convert single dash to double dash safely
        if (flag.startsWith("-") && !flag.startsWith("--")) {
            flag = flag.substring(1);
        }

        // enforce two dashes
        if (!flag.startsWith("--")) {
            flag = "--" + flag;
        }

        return flag;
    }
}
