package com.myorg.cxone.tests;

import PageObjects.ScanInfo;
import com.aventstack.extentreports.ExtentTest;
import com.myorg.cxone.helpers.Logger;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import utils.Base;
import utils.CLIHelper;
import utils.ExcelDataProvider;
import utils.ScanUtils;

import java.util.Map;

import static com.myorg.cxone.helpers.TestConstants.*;

@Listeners(utils.ExcelReportListener.class)
public class scanTestDataDriven extends Base {

    @DataProvider(name = "scanData")
    public Object[][] scanData() {
        return ExcelDataProvider.getExcelDataAsMap("src/main/resources/ScanTestData.xlsx", "Sheet1");
    }

    @Test(dataProvider = "scanData", description = "Run Checkmarx CLI Scan test from Excel")
    public void runASTCLICommandsFromExcel(Map<String, String> data) {
        ExtentTest test = getTestLogger();

        String scenarioDescription = data.get("ScenarioDescription");
        String projectNamePattern = data.get("ProjectNamePattern");
        String branch = data.get("Branch");
        String scanTypes = data.get("ScanTypes");
        String additionalFlags = data.get("AdditionalFlags");
        String expectedStatus = data.get("ExpectedStatus");
        String expectedBranch = data.get("ExpectedBranch");
        String expectedType = data.get("ExpectedType");
        String expectedEngine = data.get("ExpectedEngine");

        String logPrefix = "runASTCLICommandsFromExcel - " + scenarioDescription;
        Logger.info("--------Starting test case:------ " + logPrefix, test);

        String projectName = projectNamePattern + System.currentTimeMillis();

        String command = String.format(
                "scan create --project-name \"%s\" -s %s --branch \"%s\" --scan-types \"%s\" %s",
                projectName,
                PROJECT_PATH_ZIP,
                branch,
                scanTypes,
                (additionalFlags != null && !additionalFlags.isEmpty()) ? additionalFlags : ""
        );

        try {
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommandUntilPattern(command, OUTPUT_PATTERN, test);

            // Extract scan info
            ScanInfo scanInfo = ScanUtils.extractScanInfo(result);

            // Validate common scan info
            validateCommonScanInfo(scanInfo, projectName);

            if (expectedBranch != null && !expectedBranch.isEmpty()) {
                Assert.assertEquals(scanInfo.getBranch(), expectedBranch, "Scan branch mismatch");
            }
            if (expectedType != null && !expectedType.isEmpty()) {
                Assert.assertEquals(scanInfo.getType(), expectedType, "Scan type mismatch");
            }
            if (expectedStatus != null && !expectedStatus.isEmpty()) {
                Assert.assertEquals(scanInfo.getStatus(), expectedStatus, "Scan status mismatch");
            }
            if (expectedEngine != null && !expectedEngine.isEmpty()) {
                Assert.assertEquals(scanInfo.getEngines(), expectedEngine, "Scan engine mismatch");
            }


            Logger.pass("Test passed: " + scenarioDescription, test);

        } catch (Exception e) {
            Logger.fail("Test failed: " + scenarioDescription + " - " + e.getMessage(), test);
            Assert.fail("CLI scan failed", e);
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
    public static void validateCommonScanInfo(ScanInfo scanInfo, String projectName) {
        Assert.assertNotNull(scanInfo.getScanId(), "Scan ID should not be null");
        Assert.assertNotNull(scanInfo.getProjectId(), "Project ID should not be null");
        Assert.assertEquals(scanInfo.getStatus(), "Running", "Scan status mismatch");
        Assert.assertEquals(scanInfo.getProjectName(), projectName, "Project Name mismatch");
    }
}
