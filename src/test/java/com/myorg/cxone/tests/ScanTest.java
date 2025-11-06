/*
package com.myorg.cxone.tests;

import PageObjects.ScanInfo;
import com.aventstack.extentreports.ExtentTest;
import utils.CLIHelper;
import com.myorg.cxone.helpers.Logger;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import utils.Base;
import utils.ScanUtils;

import static com.myorg.cxone.helpers.TestConstants.*;

@Listeners(utils.ExcelReportListener.class)
public class ScanTest extends Base {

    @Test(description = "Scan List Test")
    public void scanListTest() {
        ExtentTest test = getTestLogger();

        try {
            String command = "scan list";
            String result = CLIHelper.runCommand(command);;
            Logger.info("Running CLI command: cx " + command, test);

            // Validation
            Assert.assertTrue(ScanUtils.isScanListValid(result), "Scan list did not return expected output.\n\nExpected valid scan list format.\nCLI Output:\n" + result);
            test.info("Scan List Output:\n" + command);
            test.pass("Scan list executed successfully");
            Logger.pass("Scan list fetched successfully", test);

        } catch (Exception e) {
            Logger.fail("Project creation test failed: " + e.getMessage(), test);
            Assert.fail("CLI command execution failed", e);
        }
    }

    @Test(description = "Verify Scan Count")
    public void verifyScanCountBeforeProjectCreation() {
        ExtentTest test = getTestLogger();

        try {
            String result = "scan list";
            String command = CLIHelper.runCommand(result);;
            Logger.info("Running CLI command: cx " + result, test);
            Logger.info("CLI Output:\n" + result, test);

            String scanCount = ScanUtils.getScanCountOrEmpty(command);
            Assert.assertNotNull(
                    scanCount,
                    "Scan count could not be retrieved from CLI output.\nCLI Output:\n" + result
            );
            test.info("Total scans returned before project creation: " + scanCount);
            test.pass("Scan count retrieved successfully");
            Logger.pass("Scan list count fetched successfully", test);

        } catch (Exception e) {
            Logger.fail("Project creation test failed: " + e.getMessage(), test);
            Assert.fail("CLI command execution failed", e);
        }
    }

    @Test(description = "Run and verify Checkmarx SAST scan ")
    public void createSASTScanTest() {
        ExtentTest test = getTestLogger();
        String projectName = "CLI_ScanProj_" + System.currentTimeMillis();
        String command = String.format(
                "scan create --project-name \"%s\" -s %s --branch \"master\" --scan-types \"sast\"",
                projectName, PROJECT_PATH_ZIP
        );

        try {
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommandUntilPattern(command, OUTPUT_PATTERN, test);
            ScanInfo scanInfo = ScanUtils.extractScanInfo(result);

            validateCommonScanInfo(scanInfo,projectName);
            Assert.assertEquals(scanInfo.getBranch(), "master", "Scan branch mismatch");
            Assert.assertEquals(scanInfo.getEngines(), "sast", "Scan engine mismatch");

        } catch (Exception e) {
            Logger.fail("Scan creation test failed: " + e.getMessage(), test);
            Assert.fail("CLI scan creation failed", e);
        }
    }
    @Test(description = "Run and verify Checkmarx SAST scan with relative data path")
    public void createSASTScanWithRelativePathTest() {
        ExtentTest test = getTestLogger();
        String projectName = "CLI_ScanProj_" + System.currentTimeMillis();
        String command = String.format(
                "scan create --project-name \"%s\" --file-source %s --branch \"master\" --scan-types \"sast\"",
                projectName, RELATIVE_DATA_PATH
        );

        try {
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommandUntilPattern(command, OUTPUT_PATTERN, test);
            ScanInfo scanInfo = ScanUtils.extractScanInfo(result);

            validateCommonScanInfo(scanInfo,projectName);
            Assert.assertEquals(scanInfo.getBranch(), "master", "Scan branch mismatch");
            Assert.assertEquals(scanInfo.getEngines(), "sast", "Scan engine mismatch");

        } catch (Exception e) {
            Logger.fail("Scan creation test failed: " + e.getMessage(), test);
            Assert.fail("CLI scan creation failed", e);
        }
    }

    @Test(description = "Run and verify Checkmarx SAST scan with empty project name")
    public void createSASTScanWithEmptyProjectNameTest() {
        ExtentTest test = getTestLogger();
        String projectName = "";
        String command = String.format(
                "scan create --project-name \"%s\" -s %s --branch \"master\" --scan-types \"sast\"",
                projectName, PROJECT_PATH_ZIP
        );

        try {
            Logger.info("Running CLI command with empty project name: cx " + command, test);

            String result = CLIHelper.runCommand(command);
            Logger.info("CLI Output:\n" + result, test);

            boolean hasExpectedError =
                    result.contains("Project name is required") ||
                            result.contains("Error");
            Assert.assertTrue(
                    hasExpectedError,
                    "Expected error message for empty project name not found in CLI output"
            );
            Logger.pass("CLI correctly returned an error for empty project name.", test);
        } catch (Exception e) {
            Logger.fail("Scan creation test failed: " + e.getMessage(), test);
            Assert.fail("CLI scan creation failed", e);
        }
    }

    @Test(description = "Run and verify Checkmarx SCA scan ")
    public void createSCAScanTest() {
        ExtentTest test = getTestLogger();
        String projectName = "CLI_ScanProj_" + System.currentTimeMillis();
        String command = String.format(
                "scan create --project-name \"%s\" -s %s --branch \"master\" --scan-types \"sca\"",
                projectName, PROJECT_PATH_ZIP
        );

        try {
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommandUntilPattern(command, OUTPUT_PATTERN, test);
            ScanInfo scanInfo = ScanUtils.extractScanInfo(result);

            validateCommonScanInfo(scanInfo,projectName);
            Assert.assertEquals(scanInfo.getBranch(), "master", "Scan branch mismatch");
            Assert.assertEquals(scanInfo.getEngines(), "sca", "Scan engine mismatch");

        } catch (Exception e) {
            Logger.fail("Scan creation test failed: " + e.getMessage(), test);
            Assert.fail("CLI scan creation failed", e);
        }
    }

    @Test(description = "Run and verify Checkmarx IAC scan ")
    public void createIACScanTest() {
        ExtentTest test = getTestLogger();
        String projectName = "CLI_ScanProj_" + System.currentTimeMillis();
        String command = String.format(
                "scan create --project-name \"%s\" -s %s --branch \"master\" --scan-types \"iac-security\"",
                projectName, PROJECT_PATH_ZIP
        );

        try {
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommandUntilPattern(command, OUTPUT_PATTERN, test);
            ScanInfo scanInfo = ScanUtils.extractScanInfo(result);

            validateCommonScanInfo(scanInfo,projectName);
            Assert.assertEquals(scanInfo.getBranch(), "master", "Scan branch mismatch");
            Assert.assertEquals(scanInfo.getEngines(), "kics", "Scan engine mismatch");

        } catch (Exception e) {
            Logger.fail("Scan creation test failed: " + e.getMessage(), test);
            Assert.fail("CLI scan creation failed", e);
        }
    }

    @Test(description = "Run and verify Checkmarx API scan ")
    public void createAPIScanTest() {
        ExtentTest test = getTestLogger();
        String projectName = "CLI_ScanProj_" + System.currentTimeMillis();
        String command = String.format(
                "scan create --project-name \"%s\" -s %s --branch \"master\" --scan-types \"api-security\"",
                projectName, PROJECT_PATH_ZIP
        );

        try {
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommandUntilPattern(command, OUTPUT_PATTERN, test);
            ScanInfo scanInfo = ScanUtils.extractScanInfo(result);

            validateCommonScanInfo(scanInfo,projectName);
            Assert.assertEquals(scanInfo.getBranch(), "master", "Scan branch mismatch");
            Assert.assertEquals(scanInfo.getEngines(), "apisec", "Scan engine mismatch");

        } catch (Exception e) {
            Logger.fail("Scan creation test failed: " + e.getMessage(), test);
            Assert.fail("CLI scan creation failed", e);
        }
    }

    @Test(description = "Run and verify Checkmarx Container scan ")
    public void createContainerScanTest() {
        ExtentTest test = getTestLogger();
        String projectName = "CLI_ScanProj_" + System.currentTimeMillis();
        String command = String.format(
                "scan create --project-name \"%s\" -s %s --branch \"master\" --scan-types \"container-security\"",
                projectName, PROJECT_PATH_ZIP
        );

        try {
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommandUntilPattern(command, OUTPUT_PATTERN, test);
            ScanInfo scanInfo = ScanUtils.extractScanInfo(result);

            validateCommonScanInfo(scanInfo,projectName);
            Assert.assertEquals(scanInfo.getBranch(), "master", "Scan branch mismatch");
            Assert.assertEquals(scanInfo.getEngines(), "containers", "Scan engine mismatch");

        } catch (Exception e) {
            Logger.fail("Scan creation test failed: " + e.getMessage(), test);
            Assert.fail("CLI scan creation failed", e);
        }
    }

    @Test(description = "Run and verify Checkmarx scan for all type ")
    public void createAllTypeScanTest() {
        ExtentTest test = getTestLogger();
        String projectName = "CLI_ScanProj_" + System.currentTimeMillis();
        String command = String.format(
                "scan create --project-name \"%s\" -s %s --branch \"master\"",
                projectName, PROJECT_PATH_ZIP
        );

        try {
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommandUntilPattern(command, OUTPUT_PATTERN, test);
            ScanInfo scanInfo = ScanUtils.extractScanInfo(result);

            validateCommonScanInfo(scanInfo,projectName);
            Assert.assertEquals(scanInfo.getBranch(), "master", "Scan branch mismatch");
            Assert.assertEquals(scanInfo.getEngines(), "sast kics sca apisec containers scs", "Scan engine mismatch");

        } catch (Exception e) {
            Logger.fail("Scan creation test failed: " + e.getMessage(), test);
            Assert.fail("CLI scan creation failed", e);
        }
    }

    @Test(description = "Run and verify Checkmarx SAST scan with valid application name")
    public void createSASTScanWithValidApplicationNameTest() {
        ExtentTest test = getTestLogger();
        String projectName = "CLI_ScanProj_" + System.currentTimeMillis();
        String command = String.format(
                "scan create --project-name \"%s\" -s %s --branch \"master\" --scan-types \"sast\"  --application-name \"%s\"",
                projectName, PROJECT_PATH_ZIP, VALID_APPLICATION_NAME
        );

        try {
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommandUntilPattern(command, OUTPUT_PATTERN, test);
            ScanInfo scanInfo = ScanUtils.extractScanInfo(result);

            validateCommonScanInfo(scanInfo,projectName);
            Assert.assertEquals(scanInfo.getBranch(), "master", "Scan branch mismatch");
            Assert.assertEquals(scanInfo.getEngines(), "sast", "Scan engine mismatch");

        } catch (Exception e) {
            Logger.fail("Scan creation test failed: " + e.getMessage(), test);
            Assert.fail("CLI scan creation failed", e);
        }
    }

    @Test(description = "Verify Checkmarx SAST scan fails with invalid application name")
    public void createSASTScanWithInValidApplicationNameTest() {
        ExtentTest test = getTestLogger();
        String projectName = "CLI_ScanProj_" + System.currentTimeMillis();
        String command = String.format(
                "scan create --project-name \"%s\" -s %s --branch \"master\" --scan-types \"sast\"  --application-name \"%s\"",
                projectName, PROJECT_PATH_ZIP, INVALID_APPLICATION_NAME
        );

        try {
            Logger.info("Running CLI command with invalid application name: cx " + command, test);

            String result = CLIHelper.runCommand(command);
            Logger.info("CLI Output:\n" + result, test);

            boolean hasExpectedError = result.contains("provided application does not exist") ||
                            result.contains("no permission to the application") ||
                            result.toLowerCase().contains("error");

            Assert.assertTrue(
                    hasExpectedError,
                    "Expected error for invalid application name not found in CLI output"
            );
            Logger.pass("CLI correctly returned an error for invalid application name: " + INVALID_APPLICATION_NAME, test);

        } catch (Exception e) {
            Logger.fail("Scan creation test failed: " + e.getMessage(), test);
            Assert.fail("CLI scan creation failed", e);
        }
    }
    @Test(description = "Run and verify Checkmarx SAST scan with fast scan flag enabled")
    public void createSASTScanWithFastScanEnabledTest() {
        ExtentTest test = getTestLogger();
        String projectName = "CLI_ScanProj_" + System.currentTimeMillis();
        String command = String.format(
                "scan create --project-name \"%s\" -s %s --branch \"master\" --scan-types \"sast\" --sast-fast-scan",
                projectName, PROJECT_PATH_ZIP
        );

        try {
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommandUntilPattern(command, OUTPUT_PATTERN, test);
            ScanInfo scanInfo = ScanUtils.extractScanInfo(result);

            validateCommonScanInfo(scanInfo,projectName);
            Assert.assertEquals(scanInfo.getBranch(), "master", "Scan branch mismatch");
            Assert.assertEquals(scanInfo.getEngines(), "sast", "Scan engine mismatch");

        } catch (Exception e) {
            Logger.fail("Scan creation test failed: " + e.getMessage(), test);
            Assert.fail("CLI scan creation failed", e);
        }
    }

    @Test(description = "Run scan with invalid scan type and verify error")
    public void createScanWithInvalidScanTypeTest() {
        ExtentTest test = getTestLogger();
        String projectName = "CLI_ScanProj_" + System.currentTimeMillis();
        String invalidScanType = "invalid_type";

        String command = String.format(
                "scan create --project-name \"%s\" -s %s --branch \"master\" --scan-types \"%s\"",
                projectName, PROJECT_PATH_ZIP, invalidScanType
        );

        try {
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommand(command); // normal runCommand is fine here

            Logger.info("CLI Output:\n" + result, test);

            // Assert that the CLI output contains the expected error message
            String expectedError = "It looks like the \"" + invalidScanType + "\" scan type does not exist";
            Assert.assertTrue(result.contains(expectedError),
                    "Expected error message for invalid scan type not found. CLI Output:\n" + result);

            Logger.pass("Proper error message displayed for invalid scan type: " + invalidScanType, test);

        } catch (Exception e) {
            Logger.fail("Scan test with invalid type failed: " + e.getMessage(), test);
            Assert.fail("CLI scan creation with invalid type failed", e);
        }
    }

    @Test(description = "Run Checkmarx SAST scan using API key and verify initial scan info")
    public void createScanWithValidApiKeyTest() {
        ExtentTest test = getTestLogger();
        String projectName = "CLI_ScanProj_" + System.currentTimeMillis();

        String apiKey = System.getenv("CX_APIKEY");
        Assert.assertNotNull(apiKey, "CX_APIKEY environment variable (CX_APIKEY) is not set!");
        String command = String.format(
                "scan create --project-name \"%s\" -s \"%s\" --branch \"master1\" --scan-types \"sast\" --apikey %s",
                projectName, PROJECT_PATH_ZIP, apiKey
        );

        try {
            Logger.info("Running CLI command: cx " + command, test);

            // Run command and capture output
            String result = CLIHelper.runCommand(command);
            Logger.info("CLI Output:\n" + result, test);

            // Extract scan info using your ScanUtils methods
            ScanInfo scanInfo = ScanUtils.extractScanInfo(result);

            validateCommonScanInfo(scanInfo,projectName);
            Logger.pass("Scan creation with API key executed successfully and initial scan info verified.", test);

        } catch (Exception e) {
            Logger.fail("Scan creation test with API key failed: " + e.getMessage(), test);
            Assert.fail("CLI scan creation with API key failed", e);
        }
    }

    @Test(description = "Run Checkmarx SAST scan with invalid API key and verify error")
    public void createScanWithInvalidApiKeyTest() {
        ExtentTest test = getTestLogger();
        String projectName = "CLI_ScanProj_" + System.currentTimeMillis();
        String invalidApiKey = System.getenv("CX_INVALID_APIKEY");

        Assert.assertNotNull(invalidApiKey, "CX_INVALID_APIKEY environment variable (CX_INVALID_APIKEY) is not set!");
        String command = String.format(
                "scan create --project-name \"%s\" -s \"%s\" --branch \"master1\" --scan-types \"sast\" --apikey %s",
                projectName, PROJECT_PATH_ZIP, invalidApiKey
        );

        try {
            Logger.info("Running CLI command: cx " + command, test);

            String result = CLIHelper.runCommand(command);
            Logger.info("CLI Output:\n" + result, test);

            Assert.assertTrue(result.contains("Provided credentials are invalid") ||
                            result.contains("Error validating scan types"),
                    "Expected error message for invalid API key was not found");

            Logger.pass("Scan creation with invalid API key returned expected error.", test);

        } catch (Exception e) {
            Logger.fail("Scan creation test with invalid API key failed unexpectedly: " + e.getMessage(), test);
            Assert.fail("CLI scan creation with invalid API key failed unexpectedly", e);
        }
    }

    @Test(description = "Run and verify Checkmarx SAST scan when source is folder, not zip ")
    public void createScanWithSourceAsFolderTest() {
        ExtentTest test = getTestLogger();
        String projectName = "CLI_ScanProj_" + System.currentTimeMillis();
        String command = String.format(
                "scan create --project-name \"%s\" -s %s --branch \"master\" --scan-types \"sast\"",
                projectName, PROJECT_PATH_FOLDER
        );

        try {
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommandUntilPattern(command, OUTPUT_PATTERN, test);
            ScanInfo scanInfo = ScanUtils.extractScanInfo(result);

            validateCommonScanInfo(scanInfo,projectName);

        } catch (Exception e) {
            Logger.fail("Scan creation test failed: " + e.getMessage(), test);
            Assert.fail("CLI scan creation failed", e);
        }
    }

    @Test(description = "Run Checkmarx SAST scan using Client ID and Secret (OAuth) and verify initial scan info")
    public void createScanWithClientCredentialsTest() {
        ExtentTest test = getTestLogger();
        String projectName = "CLI_Proj_OAuth_" + System.currentTimeMillis();

        String clientId = System.getenv("CX_CLIENT_ID");
        String clientSecret = System.getenv("CX_CLIENT_SECRET");

        Assert.assertNotNull(clientId, "Client ID environment variable (CX_CLIENT_ID) is not set!");
        Assert.assertNotNull(clientSecret, "Client Secret environment variable (CX_CLIENT_SECRET) is not set!");

        String command = String.format(
                "scan create --project-name \"%s\" -s \"%s\" --client-id %s --client-secret %s --branch \"master\" --scan-types \"sast\"",
                projectName, PROJECT_PATH_ZIP, clientId, clientSecret
        );

        try {
            Logger.info("Running CLI command: cx " + command, test);

            String result = CLIHelper.runCommand(command);
            Logger.info("CLI Output:\n" + result, test);

            ScanInfo scanInfo = ScanUtils.extractScanInfo(result);

            validateCommonScanInfo(scanInfo, projectName);
            Logger.pass("Scan creation with Client ID and Secret executed successfully and initial scan info verified.", test);

        } catch (Exception e) {
            Logger.fail("Scan creation test with Client ID and Secret failed: " + e.getMessage(), test);
            Assert.fail("CLI scan creation with Client ID and Secret failed", e);
        }
    }

    @Test(description = "Run Checkmarx SAST scan directly from Git repository and verify initial scan info")
    public void createScanFromGitRepoTest() {
        ExtentTest test = getTestLogger();
        String projectName = "CLI_ScanFromGit_" + System.currentTimeMillis();

        String command = String.format(
                "scan create --project-name \"%s\" -s %s --branch \"master\" --scan-types \"sast\"",
                projectName, GIT_REPO_URL
        );

        try {
            Logger.info("Running CLI command: cx " + command, test);

            String result = CLIHelper.runCommand(command);
            Logger.info("CLI Output:\n" + result, test);

            ScanInfo scanInfo = ScanUtils.extractScanInfo(result);
            validateCommonScanInfo(scanInfo, projectName);

            Logger.pass("Scan creation from Git repository executed successfully and initial scan info verified.", test);

        } catch (Exception e) {
            Logger.fail("Scan creation test from Git repository failed: " + e.getMessage(), test);
            Assert.fail("CLI scan creation from Git repository failed", e);
        }
    }

    @Test(description = "Run and verify Checkmarx SAST scan with valid project group")
    public void createSASTScanWithGroupTest() {
        ExtentTest test = getTestLogger();
        String projectName = "Proj_Group_" + System.currentTimeMillis();
        String command = String.format(
                "scan create --project-name \"%s\" -s %s --project-groups \"%s\" --scan-types \"sast\" --branch \"master\"",
                projectName, PROJECT_PATH_ZIP, VALID_GROUP_NAME
        );

        try {
            Logger.info("Running CLI command: cx " + command, test);

            String result = CLIHelper.runCommandUntilPattern(command, OUTPUT_PATTERN, test);
            ScanInfo scanInfo = ScanUtils.extractScanInfo(result);
            validateCommonScanInfo(scanInfo, projectName);

            Logger.pass("SAST scan with valid project group executed successfully for project: " + projectName, test);

        } catch (Exception e) {
            Logger.fail("Scan creation with project group failed: " + e.getMessage(), test);
            Assert.fail("CLI scan creation with project group failed", e);
        }
    }

    @Test(description = "Verify Checkmarx SAST scan fails with invalid project group")
    public void createSASTScanWithInvalidGroupTest() {
        ExtentTest test = getTestLogger();
        String projectName = "Proj_Group_Invalid_" + System.currentTimeMillis();

        String command = String.format(
                "scan create --project-name \"%s\" -s %s --project-groups \"%s\" --scan-types \"sast\" --branch \"master\"",
                projectName, PROJECT_PATH_ZIP, INVALID_GROUP_NAME
        );
        try {
            Logger.info("Running CLI command with invalid group: cx " + command, test);

            String result = CLIHelper.runCommandUntilPattern(command, OUTPUT_PATTERN, test);
            Logger.info("CLI command output:\n" + result, test);

            boolean hasError = result.contains("Failed finding groups")
                    || result.contains("Failed updating a project");
            Assert.assertTrue(hasError,
                    "Expected error message for invalid project group not found in CLI output");
            Logger.pass("CLI correctly returned an error for invalid project group: " + INVALID_GROUP_NAME, test);

        } catch (Exception e) {
            Logger.pass("CLI command failed as expected for invalid project group: " + INVALID_GROUP_NAME, test);
            Logger.info("Error details: " + e.getMessage(), test);
        }
    }

    @Test(description = "Verify CLI scan fails when project-name is missing")
    public void createSASTScanWithoutProjectNameFlagTest() {
        ExtentTest test = getTestLogger();
        String command = String.format(
                "scan create -s %s --scan-types \"sast\" --branch \"master\"",
                PROJECT_PATH_ZIP
        );

        try {
            Logger.info("Running CLI command without required project-name: cx " + command, test);

            // Execute the command (expecting failure)
            String result = CLIHelper.runCommand(command);
            Logger.info("CLI command output:\n" + result, test);

            boolean hasError = result.contains("required flag(s) \"project-name\" not set")
                    || result.contains("Error: required flag(s)");

            Assert.assertTrue(
                    hasError,
                    "Expected error for missing project-name flag not found in CLI output"
            );
            Logger.pass("CLI correctly returned an error for missing project-name flag", test);

        } catch (Exception e) {
            Logger.fail("Scan creation without project name flag failed: " + e.getMessage(), test);
            Assert.fail("CLI scan creation without project name flag failed", e);}
    }


    public static void validateCommonScanInfo(ScanInfo scanInfo, String projectName) {
        Assert.assertNotNull(scanInfo.getScanId(), "Scan ID should not be null");
        Assert.assertNotNull(scanInfo.getProjectId(), "Project ID should not be null");

        Assert.assertEquals(scanInfo.getStatus(), "Running", "Scan status mismatch");
        Assert.assertEquals(scanInfo.getProjectName(), projectName, "Project Name mismatch");
    }
}
*/
