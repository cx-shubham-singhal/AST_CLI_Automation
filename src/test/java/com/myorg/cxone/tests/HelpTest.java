package com.myorg.cxone.tests;

import com.aventstack.extentreports.ExtentTest;
import com.myorg.cxone.helpers.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.Base;
import utils.CLIHelper;
import utils.Utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class HelpTest extends Base {

    @Test(description = "CLI Help output compare")
    public void cliHelpTest() throws Exception {
        ExtentTest test = getTestLogger();
        // Path to your expected snapshot file
        Path expectedFile = Path.of("src/main/resources/cx_help.txt");

        // Run the CLI command
        try {
            String command = "--help";
            String result = CLIHelper.runCommand(command);
            Logger.info("Running CLI command: cx " + command, test);
            Logger.info("CLI Output:\n" + result, test);

            // Read expected snapshot
            String expectedOutput = Files.readString(expectedFile);

            // Normalize both outputs
            String normalizedActual = Utils.normalize(Utils.stripAnsi(result));
            String normalizedExpected = Utils.normalize(expectedOutput);

            Assert.assertEquals(normalizedActual, normalizedExpected, "CLI --help output does not match expected snapshot");
            Logger.pass("--help data is correct and matching the data from help.txt file", test);

        } catch (Exception e) {
            Logger.fail("Output mismatch: " + e.getMessage(), test);
            Assert.fail("Output mismatch", e);
        }

    }

    @Test(description = "CLI Help output compare")
    public void cliHelpTestWithoutHyphen() throws Exception {
        ExtentTest test = getTestLogger();
        // Path to your expected snapshot file
        Path expectedFile = Path.of("src/main/resources/cx_help.txt");

        // Run the CLI command
        try {
            String command = "help";
            String result = CLIHelper.runCommand(command);
            Logger.info("Running CLI command: cx " + command, test);
            Logger.info("CLI Output:\n" + result, test);

            // Read expected snapshot
            String expectedOutput = Files.readString(expectedFile);

            // Normalize both outputs
            String normalizedActual = Utils.normalize(Utils.stripAnsi(result));
            String normalizedExpected = Utils.normalize(expectedOutput);

            Assert.assertEquals(normalizedActual, normalizedExpected, "CLI --help output does not match expected snapshot");
            Logger.pass("--help data is correct and matching the data from help.txt file", test);

        } catch (Exception e) {
            Logger.fail("Output mismatch: " + e.getMessage(), test);
            Assert.fail("Output mismatch", e);
        }

    }

    @Test(description = "CLI Project Help")
    public void testProjectHelp() throws Exception {
        ExtentTest test = getTestLogger();

        try {
            String command = "project --help";
            String result = CLIHelper.runCommand(command);
            Logger.info("Running CLI command: cx " + command, test);
            Logger.info("CLI Output:\n" + result, test);

            // Call utility method for validation
            Utils.validateHelpOutput(
                    result,
                    "^The project command.*",
                    List.of("branches", "create", "delete", "list", "show", "tags"),
                    test
            );

        } catch (Exception e) {
            Logger.fail("Help output validation failed: " + e.getMessage(), test);
            Assert.fail("Help output validation failed", e);
        }
    }
    @Test(description = "CLI Project Help with -h")
    public void testProjectHelpDifferently() throws Exception {
        ExtentTest test = getTestLogger();

        try {
            String command = "project -h";
            String result = CLIHelper.runCommand(command);
            Logger.info("Running CLI command: cx " + command, test);
            Logger.info("CLI Output:\n" + result, test);

            // Call utility method for validation
            Utils.validateHelpOutput(
                    result,
                    "^The project command.*",
                    List.of("branches", "create", "delete", "list", "show", "tags"),
                    test
            );

        } catch (Exception e) {
            Logger.fail("Help output validation failed: " + e.getMessage(), test);
            Assert.fail("Help output validation failed", e);
        }
    }

    @Test(description = "CLI Scan Help")
    public void testScanHelp() throws Exception {
        ExtentTest test = getTestLogger();

        try {
            String command = "scan --help";
            String result = CLIHelper.runCommand(command);
            Logger.info("Running CLI command: cx " + command, test);
            Logger.info("CLI Output:\n" + result, test);

            // Call utility method for validation
            Utils.validateHelpOutput(
                    result,
                    "^The scan command enables the ability to manage scans.*",  // first line regex
                    List.of("cancel", "create", "delete", "kics-realtime", "list", "logs", "sca-realtime", "show", "tags", "workflow"),
                    test
            );

        } catch (Exception e) {
            Logger.fail("Help output validation failed: " + e.getMessage(), test);
            Assert.fail("Help output validation failed", e);
        }
    }

    @Test(description = "CLI Triage Help")
    public void testTriageHelp() throws Exception {
        ExtentTest test = getTestLogger();

        try {
            String command = "triage --help";
            String result = CLIHelper.runCommand(command);
            Logger.info("Running CLI command: cx " + command, test);
            Logger.info("CLI Output:\n" + result, test);

            // Call utility method for validation
            Utils.validateHelpOutput(
                    result,
                    "^The 'triage' command enables the ability to manage results.*",  // first line regex
                    List.of("get-states", "show", "update"), // commands under COMMANDS section
                    test
            );

        } catch (Exception e) {
            Logger.fail("Help output validation failed: " + e.getMessage(), test);
            Assert.fail("Help output validation failed", e);
        }
    }

    @Test(description = "CLI Configure Help")
    public void testConfigureHelp() throws Exception {
        ExtentTest test = getTestLogger();

        try {
            String command = "configure --help";
            String result = CLIHelper.runCommand(command);
            Logger.info("Running CLI command: cx " + command, test);
            Logger.info("CLI Output:\n" + result, test);

            // Call utility method for validation
            Utils.validateHelpOutput(
                    result,
                    "^The configure command is the fastest way to set up your AST CLI.*",  // first line regex
                    List.of("set", "show"), // commands under COMMANDS section
                    test
            );

        } catch (Exception e) {
            Logger.fail("Help output validation failed: " + e.getMessage(), test);
            Assert.fail("Help output validation failed", e);
        }
    }

    @Test(description = "CLI auth Help")
    public void testAuthHelp() throws Exception {
        ExtentTest test = getTestLogger();

        try {
            String command = "auth --help";
            String result = CLIHelper.runCommand(command);
            Logger.info("Running CLI command: cx " + command, test);
            Logger.info("CLI Output:\n" + result, test);

            // Call utility method for validation
            Utils.validateHelpOutput(
                    result,
                    "^Validate authentication and create OAuth2 credentials.*",  // first line regex
                    List.of("register", "validate"), // commands under COMMANDS section
                    test
            );

        } catch (Exception e) {
            Logger.fail("Help output validation failed: " + e.getMessage(), test);
            Assert.fail("Help output validation failed", e);
        }
    }

    @Test(description = "CLI project create Help")
    public void testProjectCreateHelp() throws Exception {
        ExtentTest test = getTestLogger();

        try {
            String command = "project create --help";
            String result = CLIHelper.runCommand(command);
            Logger.info("Running CLI command: cx " + command, test);
            Logger.info("CLI Output:\n" + result, test);

            Utils.validateDescriptionAndExamples(
                    result,
                    "^The project create command.*",
                    List.of("$ cx project create --project-name <Project Name>"),  // ✅ exact example line
                    test
            );

        } catch (Exception e) {
            Logger.fail("Help output validation failed: " + e.getMessage(), test);
            Assert.fail("Help output validation failed", e);
        }
    }

    @Test(description = "CLI scan create Help")
    public void testScanCreateHelp() throws Exception {
        ExtentTest test = getTestLogger();

        try {
            String command = "scan create --help";
            String result = CLIHelper.runCommand(command);
            Logger.info("Running CLI command: cx " + command, test);
            Logger.info("CLI Output:\n" + result, test);

            Utils.validateDescriptionAndExamples(
                    result,
                    "^The create command enables the ability to create and run a new scan in Checkmarx One.*",
                    List.of("$ cx scan create --project-name <Project Name> -s <path or repository url>"),
                    test
            );

        } catch (Exception e) {
            Logger.fail("Help output validation failed: " + e.getMessage(), test);
            Assert.fail("Help output validation failed", e);
        }
    }

    @Test(description = "CLI invalid Help")
    public void testInvalidHelpCommand() throws Exception {
        ExtentTest test = getTestLogger();

        String invalidCommand = "INVALID_COMMAND";
        String result = CLIHelper.runCommand(invalidCommand + " --help");

        Logger.info("Running CLI command: cx " + invalidCommand + " --help", test);
        Logger.info("CLI Output:\n" + result, test);

        String expectedError = "unknown command \"" + invalidCommand + "\" for \"cx\"";

        if (!result.contains(expectedError)) {
            Logger.fail("Expected error not found. Expected: " + expectedError, test);
            Assert.fail("Expected error not found.");
        }

        Logger.pass("Invalid command error validated successfully.", test);
    }

}