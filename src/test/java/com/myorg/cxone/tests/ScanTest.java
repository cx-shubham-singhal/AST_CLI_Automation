package com.myorg.cxone.tests;

import PageObjects.ScanInfo;
import com.aventstack.extentreports.ExtentTest;
import com.myorg.cxone.helpers.Logger;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import utils.Base;
import utils.CLIHelper;
import utils.ScanUtils;
import utils.Utils;

import static com.myorg.cxone.helpers.TestConstants.*;


@Listeners(utils.ExcelReportListener.class)
public class ScanTest extends Base {

    @Test(description = "Run and verify Checkmarx SAST scan when source is folder, not zip ")
    public void createScanWithSourceAsValidFolderTest() {
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

            ScanUtils.validateCommonScanInfo(scanInfo,projectName);
            Logger.pass("SAST Scan creation passed for source as folder", test);

            Utils.deleteProjectWithScan(scanInfo.getScanId(),scanInfo.getProjectId(),test);

        } catch (Exception e) {
            Logger.fail("SAST Scan creation test failed: " + e.getMessage(), test);
            Assert.fail("CLI scan creation failed", e);
        }
    }

    @Test(description = "Verify scan creation fails when source folder is invalid or incorrectly formatted")
    public void createScanWithInvalidSourceFolderTest() {
        ExtentTest test = getTestLogger();
        String projectName = "CLI_InvalidSrcProj_" + System.currentTimeMillis();

        String command = String.format(
                "scan create --project-name \"%s\" -s %s --branch \"master\" --scan-types \"sast\"",
                projectName,
                PROJECT_PATH_FOLDER_INVALID
        );

        Logger.info("Running CLI command: cx " + command, test);
        try {
            String result = CLIHelper.runCommand(command);

            Logger.info("CLI Output:\n" + result, test);
            Assert.assertTrue(
                    result.toLowerCase().contains("failed creating a scan") ||
                            result.toLowerCase().contains("bad format"),
                    "Expected failure message not found in CLI output"
            );

            Logger.pass("Scan creation correctly failed for invalid source folder", test);

        } catch (Exception e) {
            Logger.fail("Test execution error: " + e.getMessage(), test);
            Assert.fail("CLI execution failed unexpectedly", e);
        }
    }

    @Test(description = "Verify CLI shows proper error when scan show is executed without scan ID")
    public void showScanWithoutScanIdTest() {
        ExtentTest test = getTestLogger();
        String command = "scan show --scan-id \"\"";

        Logger.info("Running CLI command: cx " + command, test);

        try {
            String result = CLIHelper.runCommand(command);
            Logger.info("CLI Output:\n" + result, test);

            Assert.assertTrue(
                    result.toLowerCase().contains("failed showing a scan") ||
                            result.toLowerCase().contains("please provide a scan id"),
                    "Expected error message not found in CLI output"
            );
            Logger.pass("CLI correctly failed when scan show executed without scan ID", test);

        } catch (Exception e) {
            Logger.fail("Test execution error: " + e.getMessage(), test);
            Assert.fail("CLI execution failed unexpectedly", e);
        }
    }

    @Test(description = "Verify CLI throws proper error when creating a scan without providing a branch")
    public void createScanWithoutBranchTest() {
        ExtentTest test = getTestLogger();
        String projectName = "PrimaryBranch_" + System.currentTimeMillis();

        String command = String.format(
                "scan create --project-name \"%s\" -s %s --scan-types \"sast\"",
                projectName,
                PROJECT_PATH_ZIP
        );

        Logger.info("Running CLI command: cx " + command, test);

        try {
            String result = CLIHelper.runCommand(command);
            Logger.info("CLI Output:\n" + result, test);

            Assert.assertTrue(
                    result.toLowerCase().contains("failed creating a scan") ||
                            result.toLowerCase().contains("please provide a branch"),
                    "Expected branch error message not found in CLI output"
            );

            Logger.pass("CLI correctly failed when branch was not provided", test);

        } catch (Exception e) {
            Logger.fail("Test execution error: " + e.getMessage(), test);
            Assert.fail("CLI execution failed unexpectedly", e);
        }
    }

    @Test(description = "Verify scan threshold check fails when critical issues exceed the limit")
    public void verifySASTScanCriticalThresholdTest() {
        ExtentTest test = getTestLogger();
        String projectName = "CLI_ScanFromGit_" + System.currentTimeMillis();

        String command = String.format(
                "scan create --project-name \"%s\" -s %s --branch \"master\" --scan-types \"sast\" --threshold \"sast-critical=1\"",
                projectName, PROJECT_PATH_ZIP
        );

        try {
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommand(command);
            Logger.info("CLI Output:\n" + result, test);

            ScanInfo scanInfo = ScanUtils.extractScanInfo(result);
            ScanUtils.validateCommonScanInfo(scanInfo, projectName);
            Assert.assertTrue(
                    result.contains("Threshold check finished with status Failed"),
                    "Expected threshold failure message not found in CLI output.");
            Assert.assertTrue(
                    result.contains("sast-critical: Limit = 1"),
                    "Expected 'sast-critical: Limit = 1' not found in CLI output.");
            Logger.pass("Threshold check correctly failed as expected.", test);
            Utils.deleteProjectById(scanInfo.getProjectId(), test);

        } catch (Exception e) {
            Logger.fail("Error verifying threshold failure: " + e.getMessage(), test);
            Assert.fail("Unexpected CLI or assertion failure", e);
        }
    }
    @Test(description = "Verify scan threshold check fails when high issues exceed the limit")
    public void verifySASTScanHighThresholdTest() {
        ExtentTest test = getTestLogger();
        String projectName = "CLI_ScanFromGit_" + System.currentTimeMillis();

        String command = String.format(
                "scan create --project-name \"%s\" -s %s --branch \"master\" --scan-types \"sast\" --threshold \"sast-high=1\"",
                projectName, PROJECT_PATH_ZIP
        );

        try {
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommand(command);
            Logger.info("CLI Output:\n" + result, test);

            ScanInfo scanInfo = ScanUtils.extractScanInfo(result);
            ScanUtils.validateCommonScanInfo(scanInfo, projectName);
            Assert.assertTrue(
                    result.contains("Threshold check finished with status Failed"),
                    "Expected threshold failure message not found in CLI output.");
            Assert.assertTrue(
                    result.contains("sast-high: Limit = 1"),
                    "Expected 'sast-high: Limit = 1' not found in CLI output.");
            Logger.pass("Threshold check correctly failed as expected.", test);
            Utils.deleteProjectById(scanInfo.getProjectId(), test);

        } catch (Exception e) {
            Logger.fail("Error verifying threshold failure: " + e.getMessage(), test);
            Assert.fail("Unexpected CLI or assertion failure", e);
        }
    }
    @Test(description = "Verify scan threshold check fails when medium issues exceed the limit")
    public void verifySASTScanMediumThresholdTest() {
        ExtentTest test = getTestLogger();
        String projectName = "CLI_ScanFromGit_" + System.currentTimeMillis();

        String command = String.format(
                "scan create --project-name \"%s\" -s %s --branch \"master\" --scan-types \"sast\" --threshold \"sast-medium=1\"",
                projectName, PROJECT_PATH_ZIP
        );

        try {
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommand(command);
            Logger.info("CLI Output:\n" + result, test);

            ScanInfo scanInfo = ScanUtils.extractScanInfo(result);
            ScanUtils.validateCommonScanInfo(scanInfo, projectName);
            Assert.assertTrue(
                    result.contains("Threshold check finished with status Failed"),
                    "Expected threshold failure message not found in CLI output.");
            Assert.assertTrue(
                    result.contains("sast-medium: Limit = 1"),
                    "Expected 'sast-medium: Limit = 1' not found in CLI output.");
            Logger.pass("Threshold check correctly failed as expected.", test);
            Utils.deleteProjectById(scanInfo.getProjectId(), test);

        } catch (Exception e) {
            Logger.fail("Error verifying threshold failure: " + e.getMessage(), test);
            Assert.fail("Unexpected CLI or assertion failure", e);
        }
    }

    @Test(description = "Verify scan threshold check fails when issues exceed the limit")
    public void verifySASTScanThresholdTest() {
        ExtentTest test = getTestLogger();
        String projectName = "CLI_ScanFromGit_" + System.currentTimeMillis();

        String command = String.format(
                "scan create --project-name \"%s\" -s %s --branch \"master\" --scan-types \"sast\" --threshold \"sast-critical=1;sast-high=1;sast-low=1\"",
                projectName, PROJECT_PATH_ZIP
        );

        try {
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommand(command);
            Logger.info("CLI Output:\n" + result, test);

            ScanInfo scanInfo = ScanUtils.extractScanInfo(result);
            ScanUtils.validateCommonScanInfo(scanInfo, projectName);
            Assert.assertTrue(
                    result.contains("Threshold check finished with status Failed"),
                    "Expected threshold failure message not found in CLI output.");
            Logger.pass("Threshold check correctly failed as expected.", test);
            Utils.deleteProjectById(scanInfo.getProjectId(), test);

        } catch (Exception e) {
            Logger.fail("Error verifying threshold failure: " + e.getMessage(), test);
            Assert.fail("Unexpected CLI or assertion failure", e);
        }
    }

    @Test(description = "Run Checkmarx SAST scan with scan type in single quotes")
    public void createScanWithScanTypeInSingleQuotesTest() {
        ExtentTest test = getTestLogger();
        String projectName = "CLI_ScanFromGit_" + System.currentTimeMillis();

        String command = String.format(
                "scan create --project-name \"%s\" -s %s --branch \"master\" --scan-types 'sast'",
                projectName, PROJECT_PATH_ZIP
        );

        try {
            Logger.info("Running CLI command: cx " + command, test);

            String result = CLIHelper.runCommand(command);
            Logger.info("CLI Output:\n" + result, test);

            ScanInfo scanInfo = ScanUtils.extractScanInfo(result);
            ScanUtils.validateCommonScanInfo(scanInfo, projectName);

            Logger.pass("Scan creation with scan type in single quotes executed successfully and initial scan info verified.", test);
            Utils.deleteProjectWithScan(scanInfo.getScanId(),scanInfo.getProjectId(),test);
        } catch (Exception e) {
            Logger.fail("Scan creation test with scan type in single quotes failed: " + e.getMessage(), test);
            Assert.fail("CLI scan creation with scan type in single quotes failed", e);
        }
    }

    @Test(description = "Run Checkmarx scan with multiple scan type in single quotes")
    public void createScanWithScanTypeInSingleQuotesAndMultipleScanTypeTest() {
        ExtentTest test = getTestLogger();
        String projectName = "CLI_ScanFromGit_" + System.currentTimeMillis();

        String command = String.format(
                "scan create --project-name \"%s\" -s %s --branch \"master\" --scan-types 'sast,sca'",
                projectName, PROJECT_PATH_ZIP
        );

        try {
            Logger.info("Running CLI command: cx " + command, test);

            String result = CLIHelper.runCommand(command);
            Logger.info("CLI Output:\n" + result, test);

            ScanInfo scanInfo = ScanUtils.extractScanInfo(result);
            ScanUtils.validateCommonScanInfo(scanInfo, projectName);

            Logger.pass("Scan creation with multiple scan type in single quotes executed successfully and initial scan info verified.", test);
            Utils.deleteProjectWithScan(scanInfo.getScanId(),scanInfo.getProjectId(),test);
        } catch (Exception e) {
            Logger.fail("Scan creation test with multiple scan type in single quotes failed: " + e.getMessage(), test);
            Assert.fail("CLI scan creation with multiple scan type in single quotes failed", e);
        }
    }
}
