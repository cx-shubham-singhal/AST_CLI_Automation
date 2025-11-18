package utils;
import com.aventstack.extentreports.ExtentTest;
import com.myorg.cxone.helpers.Logger;
import org.testng.Assert;

import java.util.*;
import java.util.stream.Collectors;

public class Utils extends Base{

    private Utils() {}


    public static String normalize(String input) {
        return Arrays.stream(input.split("\\r?\\n"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .collect(Collectors.joining("\n"));
    }

    public static String stripAnsi(String input) {
        return input.replaceAll("\u001B\\[[;\\d]*m", "");
    }

    public static void validateHelpOutput(
            String cliOutput,
            String firstLinePattern,
            List<String> expectedCommands,
            ExtentTest test
    ) {
        String normalized = Utils.normalize(Utils.stripAnsi(cliOutput));
        String[] lines = normalized.split("\\r?\\n");

        // 1️⃣ Validate first line
        if (!lines[0].matches(firstLinePattern)) {
            String msg = "Expected description: " + firstLinePattern
                    + "\nActual description: " + lines[0];
            Logger.fail(msg, test);
            Assert.fail(msg);
        }

        // 2️⃣ Extract commands under COMMANDS section ONLY
        int commandsStart = -1;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().equalsIgnoreCase("COMMANDS")) {
                commandsStart = i + 1;
                break;
            }
        }

        List<String> actualCommands = new ArrayList<>();
        if (commandsStart != -1) {
            for (int i = commandsStart; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty() || line.equalsIgnoreCase("FLAGS") || line.equalsIgnoreCase("GLOBAL FLAGS")) {
                    break; // stop at end of COMMANDS section
                }
                if (line.contains(":")) {
                    actualCommands.add(line.split(":")[0].trim());
                }
            }
        }

        // 3️⃣ Compare actual vs expected commands
        Set<String> actualSet = new HashSet<>(actualCommands);
        Set<String> expectedSet = new HashSet<>(expectedCommands);

        if (!actualSet.equals(expectedSet)) {
            String msg = "CLI commands do not match expected commands."
                    + "\nExpected: " + expectedSet
                    + "\nActual  : " + actualSet;
            Logger.fail(msg, test);
            Assert.fail(msg);
        }

        Logger.pass("First line and commands validated successfully.", test);
    }

    public static void validateDescriptionAndExamples(
            String cliOutput,
            String firstLinePattern,
            List<String> expectedExamples,
            ExtentTest test
    ) {
        String normalized = Utils.normalize(Utils.stripAnsi(cliOutput));
        String[] lines = normalized.split("\\r?\\n");

        if (lines.length == 0) {
            Logger.fail("CLI output is empty!", test);
            Assert.fail("CLI output is empty!");
        }

        if (!lines[0].matches(firstLinePattern)) {
            String msg = "Description does not match expected pattern."
                    + "\nExpected: " + firstLinePattern
                    + "\nActual  : " + lines[0];
            Logger.fail(msg, test);
            Assert.fail(msg);
        }

        List<String> actualExamples = new ArrayList<>();
        int examplesStart = -1;

        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().equalsIgnoreCase("EXAMPLES")) {
                examplesStart = i + 1;
                break;
            }
        }

        if (examplesStart != -1) {
            for (int i = examplesStart; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty() || line.equalsIgnoreCase("DOCUMENTATION") || line.equalsIgnoreCase("LEARN MORE")) {
                    break; // End EXAMPLES section
                }
                actualExamples.add(line);
            }
        }

        Set<String> actualSet = new HashSet<>(actualExamples);
        Set<String> expectedSet = new HashSet<>(expectedExamples);

        if (!actualSet.equals(expectedSet)) {
            String msg = "EXAMPLES section does not match expected."
                    + "\nExpected: " + expectedSet
                    + "\nActual  : " + actualSet;
            Logger.fail(msg, test);
            Assert.fail(msg);
        }

        Logger.pass("Description and EXAMPLES validated successfully.", test);
    }

    public static void cancelScanById(String scanId, ExtentTest test) throws Exception {
        String cancelCommand = String.format("scan cancel --scan-id %s", scanId);
        Logger.info("Running CLI command: cx " + cancelCommand, test);
        CLIHelper.runCommand(cancelCommand);
        Logger.info("Scan command executed successfully", test);
    }

    public static void deleteProjectById(String projectId, ExtentTest test) throws Exception {
        String deleteCommand = String.format("project delete --project-id %s", projectId);

        Logger.info("Running CLI command: cx " + deleteCommand, test);
        CLIHelper.runCommand(deleteCommand);
        Logger.info("Project delete command executed. Verifying deletion...", test);
        boolean isDeleted = isProjectDeleted(projectId, test);
        Assert.assertTrue(isDeleted,
                "Project with ID " + projectId + " still appears after deletion!"
        );
        Logger.info("Project with ID " + projectId + " deleted successfully.", test);
    }

    public static boolean isProjectDeleted(String projectId, ExtentTest test) throws Exception {
        String showCommand = String.format("project show --project-id %s", projectId);
        Logger.info("Running CLI command: cx " + showCommand, test);
        String result = CLIHelper.runCommand(showCommand);

        boolean notFound = result.toLowerCase().contains("project not found")
                || result.toLowerCase().contains("failed getting a project");

        Logger.info("Verification after delete. CLI show flag Response:\n" + result, test);

        return notFound;
    }

    public static void deleteProjectWithScan(String scanId,String projectId,  ExtentTest test) throws Exception {
        cancelScanById(scanId,test);
        deleteProjectById(projectId,test);
    }

}