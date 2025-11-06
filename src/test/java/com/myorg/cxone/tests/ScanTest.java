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

            validateCommonScanInfo(scanInfo,projectName);

        } catch (Exception e) {
            Logger.fail("Scan creation test failed: " + e.getMessage(), test);
            Assert.fail("CLI scan creation failed", e);
        }
    }

    @Test(description = "Verify scan creation fails when source folder is invalid or incorrectly formatted")
    public void createScanWithInvalidSourceFolderTest() {
        ExtentTest test = getTestLogger();
        String projectName = "CLI_InvalidSrcProj_" + System.currentTimeMillis();

        String invalidSourcePath = "src/main/resources/JavaVulnerableLabE"; // no '-master.zip'

        String command = String.format(
                "scan create --project-name \"%s\" -s %s --branch \"master\" --scan-types \"sast\"",
                projectName,
                PROJECT_PATH_FOLDER_INVALID
        );

        Logger.info("Running CLI command: cx " + command, test);
        try {
            String result = CLIHelper.runCommand(command);

            Logger.info("CLI Output:\n" + result, test);

            // ✅ Assert that expected error message appears
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
            // Run the command (expected to fail)
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


    public static void validateCommonScanInfo(ScanInfo scanInfo, String projectName) {
        Assert.assertNotNull(scanInfo.getScanId(), "Scan ID should not be null");
        Assert.assertNotNull(scanInfo.getProjectId(), "Project ID should not be null");

        Assert.assertEquals(scanInfo.getStatus(), "Running", "Scan status mismatch");
        Assert.assertEquals(scanInfo.getProjectName(), projectName, "Project Name mismatch");
    }
}
