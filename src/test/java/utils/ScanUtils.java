package utils;

import PageObjects.ScanInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScanUtils {

    public static boolean isScanListValid(String result) {
        return result != null && result.contains("ID");
    }

    public static void printScanList(String result) {
        System.out.println(result);
    }

    public static String getScanCountOrEmpty(String command) {
        String[] lines = command.split("\\r?\\n");

        // Assuming first 2 lines are headers
        int count = Math.max(0, lines.length - 2);

        return count == 0 ? "empty" : String.valueOf(count-1);
    }

    public static ScanInfo extractScanInfo(String cliOutput) {
        String scanId = extractValue(cliOutput, "Scan ID");
        String projectId = extractValue(cliOutput, "Project ID");
        String projectName = extractValue(cliOutput, "Project Name");
        String status = extractValue(cliOutput, "Status");
        String branch = extractValue(cliOutput, "Branch");
        String type = extractValue(cliOutput, "Type");
        String engines = extractValue(cliOutput, "Engines", true); // special handling for brackets

        return new ScanInfo(scanId, projectId, projectName, status, branch, type, engines);
    }

    // Helper method to extract single-line values
    private static String extractValue(String cliOutput, String key) {
        return extractValue(cliOutput, key, false);
    }

    private static String extractValue(String cliOutput, String key, boolean isBracketed) {
        String patternStr;
        if (isBracketed) {
            patternStr = key + "\\s*:\\s*\\[(.*?)\\]";
        } else {
            patternStr = key + "\\s*:\\s*(.+)";
        }

        Matcher matcher = Pattern.compile(patternStr).matcher(cliOutput);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

}
