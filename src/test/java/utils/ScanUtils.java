package utils;

import PageObjects.ScanInfo;
import com.aventstack.extentreports.ExtentTest;
import com.myorg.cxone.helpers.Logger;
import com.myorg.cxone.helpers.TestConstants;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.myorg.cxone.helpers.TestConstants.GIT_REPO_URL;

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

        additionalFlags = additionalFlags.trim();

        // 🔹 Remove leading backtick ONLY if present
        if (additionalFlags.startsWith("`")) {
            additionalFlags = additionalFlags.substring(1).trim();
        }

        // 🔹 Preserve double-dash flags (DO NOT strip characters blindly)
        if (additionalFlags.contains("%s")) {
            if (additionalFlags.contains("--apikey")) {
                additionalFlags = String.format(
                        additionalFlags,
                        TestConstants.CX_API_KEY
                );
            } else if (additionalFlags.contains("--client-id")
                    && additionalFlags.contains("--client-secret")) {

                additionalFlags = String.format(
                        additionalFlags,
                        TestConstants.CX_CLIENT_ID,
                        TestConstants.CX_CLIENT_SECRET
                );
            } else {
                throw new RuntimeException(
                        "Unrecognized credential placeholder pattern in AdditionalFlags: "
                                + additionalFlags
                );
            }
        }

        return additionalFlags;
    }


    private String extractProjectId(String cliOutput) {
        Matcher matcher = Pattern.compile("[a-fA-F0-9\\-]{36}").matcher(cliOutput);
        return matcher.find() ? matcher.group() : null;
    }

    public static void validateCommonScanInfo(ScanInfo scanInfo, String projectName) {
        Assert.assertNotNull(scanInfo.getScanId(), "Scan ID should not be null");
        Assert.assertNotNull(scanInfo.getProjectId(), "Project ID should not be null");
        Assert.assertEquals(scanInfo.getStatus(), "Running", "Scan status mismatch");
        Assert.assertEquals(scanInfo.getProjectName(), projectName, "Project Name mismatch");
    }
}
