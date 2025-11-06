package utils;

import PageObjects.ScanInfo;
import com.myorg.cxone.helpers.TestConstants;

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

    public static String resolveAdditionalFlags(String additionalFlags) {
        if (additionalFlags == null || additionalFlags.trim().isEmpty()) {
            return "";
        }

        additionalFlags = additionalFlags.substring(1).trim();

        if (additionalFlags.contains("%s")) {
            if (additionalFlags.contains("--apikey")) {
                additionalFlags = String.format(additionalFlags, TestConstants.CX_API_KEY);
            } else if (additionalFlags.contains("--client-id") && additionalFlags.contains("--client-secret")) {
                additionalFlags = String.format(
                        additionalFlags,
                        TestConstants.CX_CLIENT_ID,
                        TestConstants.CX_CLIENT_SECRET
                );
            } else {
                throw new RuntimeException(
                        "Unrecognized credential placeholder pattern in AdditionalFlags: " + additionalFlags
                );
            }
        }

        return additionalFlags;
    }

}
