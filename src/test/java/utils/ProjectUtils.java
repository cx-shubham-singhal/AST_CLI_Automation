package utils;

import com.aventstack.extentreports.ExtentTest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.myorg.cxone.helpers.Logger;
import org.testng.Assert;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class ProjectUtils
{
    public static String showProjectById(String projectId, String format, ExtentTest test) throws Exception {
        String command = String.format("project show --project-id %s", projectId);
        if (format != null && !format.isEmpty()) {
            command += " --format " + format;
        }

        String result = CLIHelper.runCommand(command);
        Logger.info("CLI Output:\n" + result, test);
        boolean matches = result.toLowerCase().contains(projectId.toLowerCase());

        Assert.assertTrue(matches,
                "SHOW command did not return the expected project!\nExpected Project ID: "
                        + projectId + "\nCLI Output:\n" + result);

        Logger.pass("Project with ID " + projectId + " successfully verified in SHOW command (" + format + " format).", test);

        return result;
    }

    public static String detectOutputFormat(String result, ExtentTest test) {
        String trimmed = result.trim();

        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            Logger.info("Detected output format: JSON", test);
            return "json";
        }

        if (trimmed.contains(":") && !trimmed.contains("----")) {
            String[] lines = trimmed.split("\\r?\\n");
            int kvCount = 0;
            for (String line : lines) {
                if (line.matches(".*\\s+:\\s+.*")) {
                    kvCount++;
                }
            }
            if (kvCount >= 3) {
                Logger.info("Detected output format: LIST", test);
                return "list";
            }
        }

        if (trimmed.contains("----------") || trimmed.matches("(?s).*\\n[-\\s]+\\n.*")) {
            Logger.info("Detected output format: TABLE", test);
            return "table";
        }

        Logger.info("Could not clearly detect output format — marking as UNKNOWN", test);
        return "unknown";
    }

    public static void deleteProjectById(String projectId, ExtentTest test) throws Exception {
        String deleteCommand = String.format("project delete --project-id %s", projectId);

        CLIHelper.runCommand(deleteCommand);
        Logger.info("Project delete command executed. Verifying deletion...", test);

        boolean isDeleted = isProjectDeleted(projectId, test);

        Assert.assertTrue(isDeleted,
                "Project with ID " + projectId + " still appears after deletion!"
        );

        Logger.pass("Project with ID " + projectId + " deleted successfully.", test);
    }


    public static boolean isProjectDeleted(String projectId, ExtentTest test) throws Exception {
        String command = String.format("project show --project-id %s", projectId);
        String result = CLIHelper.runCommand(command);

        boolean notFound = result.toLowerCase().contains("project not found")
                || result.toLowerCase().contains("failed getting a project");

        Logger.info("Verification after delete. CLI show flag Response:\n" + result, test);

        return notFound;
    }

    public static List<Map<String, Object>> parseProjectsJson(String jsonOutput, ExtentTest test) {
        Logger.info("CLI output is in JSON format.", test);
        Logger.info("JSON Output:\n" + jsonOutput, test);

        Gson gson = new Gson();
        Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
        List<Map<String, Object>> projects = gson.fromJson(jsonOutput, listType);

        Logger.pass("CLI JSON parsed successfully. Total count:  " + projects.size(), test);
        return projects;
    }

    public static Map<String, List<String>> parseTagsJson(String jsonOutput, ExtentTest test) {
        Logger.info("CLI output is in JSON format.", test);
        Logger.info("JSON Output:\n" + jsonOutput, test);

        Gson gson = new Gson();
        Type mapType = new TypeToken<Map<String, List<String>>>() {}.getType();
        Map<String, List<String>> tagsMap = gson.fromJson(jsonOutput, mapType);

        int totalPairs = 0;
        for (Map.Entry<String, List<String>> entry : tagsMap.entrySet()) {
            // If the list is empty, consider it as a single null-value tag
            totalPairs += Math.max(entry.getValue().size(), 1);
        }

        Logger.info("Parsed tags from JSON. Total tag key-value pairs: " + totalPairs, test);

        return tagsMap;
    }
}

