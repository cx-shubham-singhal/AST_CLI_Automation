package com.myorg.cxone.tests;

import PageObjects.ScanInfo;
import com.aventstack.extentreports.ExtentTest;
import com.myorg.cxone.helpers.ExcelDataProvider;
import com.myorg.cxone.helpers.Logger;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import utils.Base;
import utils.CLIHelper;
import utils.ScanUtils;
import utils.Utils;

import java.util.Map;

import static com.myorg.cxone.helpers.TestConstants.*;

public class scanTestDataDriven extends Base {

    @DataProvider(name = "scanData")
    public Object[][] scanData() {
        return ExcelDataProvider.getExcelDataAsMap("src/main/resources/ScanTestData.xlsx", "ScanSheet");
    }

    @Test(dataProvider = "scanData", description = "Run Checkmarx CLI Scan test from Excel")
    public void runASTCLICommandsFromExcelTests(Map<String, String> data) {
        ExtentTest test = getTestLogger();

        String scenarioDescription = data.get("ScenarioDescription");
        String scanTypes = data.get("ScanTypes");
        String additionalFlags = ScanUtils.resolveAdditionalFlags(data.get("AdditionalFlags"));
        String expectedStatus = data.get("ExpectedStatus");
        String expectedBranch = data.get("ExpectedBranch");
        String expectedType = data.get("ExpectedType");
        String expectedEngine = data.get("ExpectedEngine");
        String expectedError = data.get("ExpectedError");

        String logPrefix = "runASTCLICommandsFromExcel - " + scenarioDescription;
        Logger.info("--------Starting test case:------ " + logPrefix, test);

        String projectName = "CLI_ScanProj_" + System.currentTimeMillis();
        String command = String.format(
                "scan create --project-name \"%s\" -s %s --branch \"master\" --scan-types \"%s\" %s",
                projectName,
                PROJECT_PATH_ZIP,
                scanTypes,
                (additionalFlags != null && !additionalFlags.isEmpty()) ? additionalFlags : ""
        );

        try {
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommandUntilPattern(command, OUTPUT_PATTERN, test);

            if (expectedError != null && !expectedError.isEmpty()) {
                Logger.info("Validating expected error...", test);
                Assert.assertTrue(
                        result.toLowerCase().contains(expectedError.toLowerCase()),
                        "Expected error not found. Actual output:\n" + result
                );
                Logger.pass("Expected error found: " + expectedError, test);
                return; // ✅ stop here, don’t proceed to positive validations
            }


            ScanInfo scanInfo = ScanUtils.extractScanInfo(result);
            validateBulkScanInfo(scanInfo, projectName, expectedBranch, expectedType, expectedStatus, expectedEngine);

            Logger.pass("Test passed: " + scenarioDescription, test);
            Utils.deleteProjectWithScan(scanInfo.getScanId(),scanInfo.getProjectId(),test);
        } catch (Exception e) {
            Logger.fail("Test failed: " + scenarioDescription + " - " + e.getMessage(), test);
            Assert.fail("CLI scan failed", e);
        }
    }

    @Test(description = "Verify All scan ")
    public void verifySASTScanWithAllScanTypes() {
        ExtentTest test = getTestLogger();
        String projectName = "CLI_ScanProj_" + System.currentTimeMillis();

        String command = String.format(
                "scan create --project-name \"%s\" -s %s --branch \"master\" --scan-types \"\"",
                projectName, PROJECT_PATH_ZIP
        );

        try {
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommand(command);
            Logger.info("CLI Output:\n" + result, test);

            ScanInfo scanInfo = ScanUtils.extractScanInfo(result);
            ScanUtils.validateCommonScanInfo(scanInfo, projectName);

            Logger.pass("Scan creation with all scan types verified and initial scan info verified.", test);
            Utils.deleteProjectById(scanInfo.getProjectId(),test);
        } catch (Exception e) {
            Logger.fail("All scan type Scan creation test failed: " + e.getMessage(), test);
            Assert.fail("All scan type Scan creation test failed", e);
        }
    }

    @Test(description = "Scan List Test")
    public void scanListTest() {
        ExtentTest test = getTestLogger();

        try {
            String command = "scan list";
            String result = CLIHelper.runCommand(command);;
            Logger.info("Running CLI command: cx " + command, test);
            Logger.info("CLI Output:\n" + result, test);

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
            String command = "scan list";
            String result = CLIHelper.runCommand(command);;
            Logger.info("Running CLI command: cx " + command, test);
            Logger.info("CLI Output:\n" + result, test);

            String scanCount = ScanUtils.getScanCountOrEmpty(result);
            Logger.info("Scan Count:\n" + scanCount, test);
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

            ScanUtils.validateCommonScanInfo(scanInfo, projectName);
            Assert.assertEquals(scanInfo.getBranch(), "master", "Scan branch mismatch");
            Assert.assertEquals(scanInfo.getEngines(), "sast", "Scan engine mismatch");
            Utils.deleteProjectWithScan(scanInfo.getScanId(),scanInfo.getProjectId(),test);
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
            ScanUtils.validateCommonScanInfo(scanInfo, projectName);

            Logger.pass("Scan creation from Git repository executed successfully and initial scan info verified.", test);
             Utils.deleteProjectById(scanInfo.getProjectId(),test);
        } catch (Exception e) {
            Logger.fail("Scan creation test from Git repository failed: " + e.getMessage(), test);
            Assert.fail("CLI scan creation from Git repository failed", e);
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

    public static void validateBulkScanInfo(
            ScanInfo scanInfo,
            String projectName,
            String expectedBranch,
            String expectedType,
            String expectedStatus,
            String expectedEngine
    ) {
        ScanUtils.validateCommonScanInfo(scanInfo, projectName);
        if (expectedBranch != null && !expectedBranch.isEmpty()) {
            Assert.assertEquals(scanInfo.getBranch(), expectedBranch, "Scan branch mismatch");
        }

        if (expectedType != null && !expectedType.isEmpty()) {
            Assert.assertEquals(scanInfo.getType(), expectedType, "Scan type mismatch");
        }

        if (expectedStatus != null && !expectedStatus.isEmpty()) {
            Assert.assertEquals(scanInfo.getStatus(), expectedStatus, "Scan status mismatch");
        } else {
            Assert.assertEquals(scanInfo.getStatus(), "Running", "Scan status mismatch");
        }

        if (expectedEngine != null && !expectedEngine.isEmpty()) {
            Assert.assertEquals(scanInfo.getEngines(), expectedEngine, "Scan engine mismatch");
        }
    }
}
