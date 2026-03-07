package com.myorg.cxone.tests;

import PageObjects.ScanInfo;
import com.aventstack.extentreports.ExtentTest;
import com.myorg.cxone.helpers.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.Base;
import utils.CLIHelper;
import utils.ScanUtils;
import utils.Utils;

import java.util.List;

import static com.myorg.cxone.helpers.TestConstants.*;

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
                projectName, PROJECT_PATH_ZIP_JAVAVUL
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
                projectName, PROJECT_PATH_ZIP_JAVAVUL
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
                projectName, PROJECT_PATH_ZIP_JAVAVUL
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
                projectName, PROJECT_PATH_ZIP_JAVAVUL
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

    @Test(description = "Verify multiple application names can be attached to same project")
    public void verifyMultipleApplicationNamesForSameProject() {
        ExtentTest test = getTestLogger();
        String projectName = "MultipleAppProj_" + System.currentTimeMillis();
        String appName1 = "QAApplicationForAutomation";
        String appName2 = "CliAutomationApplication";

        String firstScanCmd = String.format(
                "scan create --project-name \"%s\" -s %s --branch \"master\" --scan-types \"sast\" --application-name \"%s\"",
                projectName, PROJECT_PATH_ZIP, appName1);
        String secondScanCmd = String.format(
                "scan create --project-name \"%s\" -s %s --branch \"master\" --scan-types \"sast\" --application-name \"%s\"",
                projectName, PROJECT_PATH_ZIP, appName2);
        try {
            Logger.info("Running first CLI command: cx " + firstScanCmd, test);
            String firstResult = CLIHelper.runCommandUntilPattern(firstScanCmd, OUTPUT_PATTERN, test);
            Logger.info("First Scan Output:\n" + firstResult, test);

            ScanInfo firstScanInfo = ScanUtils.extractScanInfo(firstResult);
            ScanUtils.validateCommonScanInfo(firstScanInfo, projectName);
            Logger.pass("First scan created successfully with application: " + appName1, test);

            Logger.info("Running second CLI command: cx " + secondScanCmd, test);
            String secondResult = CLIHelper.runCommandUntilPattern(secondScanCmd, OUTPUT_PATTERN, test);
            Logger.info("Second Scan Output:\n" + secondResult, test);

            ScanInfo secondScanInfo = ScanUtils.extractScanInfo(secondResult);
            ScanUtils.validateCommonScanInfo(secondScanInfo, projectName);
            Logger.pass("Second scan created successfully with application: " + appName2, test);

            String projectShowResult = Utils.showProject(firstScanInfo.getProjectId(), test);

            List<String> applicationIds = ScanUtils.extractApplicationIds(projectShowResult);
            Assert.assertNotNull(applicationIds, "ApplicationIds must not be null");
            Assert.assertEquals(applicationIds.size(), 2,
                    "Project must contain exactly two applications");

            Logger.pass("Project successfully contains two application entries: " + applicationIds, test);
            Utils.deleteProjectById(firstScanInfo.getProjectId(), test);

        } catch (Exception e) {
            Logger.fail("Multiple Application Name Test failed: " + e.getMessage(), test);
            Assert.fail("Multiple Application Name Test failed", e);
        }
    }
    @Test(description = "Verify API Security scan displays full documentation-only disclaimer")
    public void verifyApiSecurityScanDisclaimerTest() {

        ExtentTest test = getTestLogger();
        String projectName = "CLI_ApiSecScan_" + System.currentTimeMillis();

        String command = String.format(
                "scan create --project-name \"%s\" -s %s --branch \"master\" --scan-types \"api-security\"",
                projectName, PROJECT_PATH_ZIP
        );

        try {
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommand(command);
            Logger.info("CLI Output:\n" + result, test);

            ScanInfo scanInfo = ScanUtils.extractScanInfo(result);
            ScanUtils.validateCommonScanInfo(scanInfo, projectName);

            Assert.assertTrue(
                    result.contains("Scan Finished with status:  Completed"),
                    "Scan did not complete successfully."
            );

            String normalizedOutput = result.replaceAll("\\s+", " ").trim();

            String expectedStatement =
                    "Total Results includes only API documentation vulnerabilities and does not include API code vulnerabilities.";

            Assert.assertTrue(
                    normalizedOutput.contains(expectedStatement),
                    "Expected full API Security disclaimer not found in CLI output."
            );

            Logger.pass("Full API Security disclaimer verified successfully.", test);
            Utils.deleteProjectById(scanInfo.getProjectId(), test);

        } catch (Exception e) {
            Logger.fail("Error verifying API Security disclaimer: " + e.getMessage(), test);
            Assert.fail("Unexpected CLI or assertion failure", e);
        }
    }

    @Test(description = "Verify NEW filter returns vulnerabilities only on first scan and zero on subsequent scans")
    public void verifyNewFilterBehaviorTest() {

        ExtentTest test = getTestLogger();
        String projectName = "NewFilterProj_" + System.currentTimeMillis();

        String command = String.format(
                "scan create --project-name \"%s\" --branch master -s \"%s\" --filter \"status=NEW\"",
                projectName, PROJECT_PATH_ZIP);

        try {

            Logger.info("Running CLI command: cx " + command, test);
            String firstResult = CLIHelper.runCommand(command);
            Logger.info("First Scan Output:\n" + firstResult, test);

            int firstTotal = ScanUtils.extractTotalResults(firstResult);

            Assert.assertTrue(firstTotal > 0,
                    "First scan should return NEW vulnerabilities but returned 0");
            Logger.pass("First scan detected NEW vulnerabilities: " + firstTotal, test);
            ScanInfo firstScanInfo = ScanUtils.extractScanInfo(firstResult);
            ScanUtils.validateCommonScanInfo(firstScanInfo, projectName);

            // -------- SECOND SCAN --------
            Logger.info("Running CLI command: cx " + command, test);
            String secondResult = CLIHelper.runCommand(command);
            Logger.info("Second Scan Output:\n" + secondResult, test);
            int secondTotal = ScanUtils.extractTotalResults(secondResult);
            ScanInfo secondScanInfo = ScanUtils.extractScanInfo(secondResult);
            ScanUtils.validateCommonScanInfo(secondScanInfo, projectName);

            Assert.assertEquals(secondTotal, 0,
                    "Second scan should return 0 results since vulnerabilities are now recurrent");
            Logger.pass("Second scan correctly returned 0 results after recurrence filtering", test);


            // cleanup
            ScanInfo scanInfo = ScanUtils.extractScanInfo(firstResult);
            Utils.deleteProjectById(scanInfo.getProjectId(), test);

        } catch (Exception e) {
            Logger.fail("NEW filter behavior test failed: " + e.getMessage(), test);
            Assert.fail("Unexpected CLI failure", e);
        }
    }
    @Test(description = "Verify threshold failure for API Security medium vulnerabilities with NEW filter")
    public void verifyApiSecurityThresholdFailureTest() {

        ExtentTest test = getTestLogger();
        String projectName = "ThresholdFilterProj_" + System.currentTimeMillis();

        String command = String.format(
                "scan create --project-name \"%s\" --branch master -s \"%s\" --filter \"status=NEW\" --threshold \"api-security-medium=1\"",
                projectName, PROJECT_PATH_ZIP);

        try {

            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommand(command);
            Logger.info("CLI Output:\n" + result, test);

            ScanInfo scanInfo = ScanUtils.extractScanInfo(result);
            ScanUtils.validateCommonScanInfo(scanInfo, projectName);

            Assert.assertTrue(
                    result.contains("Scan Finished with status:  Completed"),
                    "Scan did not complete successfully");

            // Assertion for threshold failure
            String expectedMessage =
                    "Threshold check finished with status Failed : api-security-medium: Limit = 1";

            Assert.assertTrue(
                    result.contains(expectedMessage),
                    "Expected threshold failure message not found in CLI output");

            Logger.pass("API Security threshold failure correctly detected.", test);

            Utils.deleteProjectById(scanInfo.getProjectId(), test);

        } catch (Exception e) {
            Logger.fail("Threshold validation test failed: " + e.getMessage(), test);
            Assert.fail("Unexpected CLI or assertion failure", e);
        }
    }

    @Test(description = "Verify threshold passes when NEW vulnerabilities become recurrent")
    public void verifyThresholdWithNewFilterAfterInitialScan() {

        ExtentTest test = getTestLogger();
        String projectName = "ThresholdRecurrentProj_" + System.currentTimeMillis();

        String firstCommand = String.format(
                "scan create --project-name \"%s\" --branch master -s \"%s\" --filter \"status=NEW\"",
                projectName, PROJECT_PATH_ZIP);

        String secondCommand = String.format(
                "scan create --project-name \"%s\" --branch master -s \"%s\" --filter \"status=NEW\" --threshold \"api-security-medium=1\"",
                projectName, PROJECT_PATH_ZIP);

        try {

            Logger.info("Running first CLI command: cx " + firstCommand, test);
            String firstResult = CLIHelper.runCommand(firstCommand);
            Logger.info("First Scan Output:\n" + firstResult, test);

            ScanInfo firstScanInfo = ScanUtils.extractScanInfo(firstResult);
            ScanUtils.validateCommonScanInfo(firstScanInfo, projectName);
            int firstTotal = ScanUtils.extractTotalResults(firstResult);
            Assert.assertTrue(firstTotal > 0,
                    "First scan should detect NEW vulnerabilities.");
            Logger.pass("First scan detected NEW vulnerabilities: " + firstTotal, test);

            Logger.info("Running second CLI command: cx " + secondCommand, test);
            String secondResult = CLIHelper.runCommand(secondCommand);
            Logger.info("Second Scan Output:\n" + secondResult, test);
            ScanInfo secondScanInfo = ScanUtils.extractScanInfo(secondResult);

            Assert.assertEquals(
                    secondScanInfo.getProjectId(),
                    firstScanInfo.getProjectId(),
                    "Second scan must run on the same project");

            int secondTotal = ScanUtils.extractTotalResults(secondResult);
            Assert.assertEquals(secondTotal, 0,
                    "Second scan should return zero results because vulnerabilities became recurrent");

            Assert.assertTrue(
                    secondResult.contains("Threshold check finished with status Success"),
                    "Threshold should pass because filtered results are zero");

            Logger.pass("Threshold check passed correctly after vulnerabilities became recurrent.", test);
            Utils.deleteProjectById(firstScanInfo.getProjectId(), test);

        } catch (Exception e) {
            Logger.fail("Threshold recurrence test failed: " + e.getMessage(), test);
            Assert.fail("Unexpected CLI failure", e);
        }
    }

    @Test(description = "Verify apisec swagger filter honors case sensitivity by comparing file exclusion vs inclusion")
    public void verifyApiSecSwaggerFilterCaseSensitivity() {

        ExtentTest test = getTestLogger();
        String projectName = "ApiSwaggerCaseTest_" + System.currentTimeMillis();

        String excludeFileCommand = String.format(
                "scan create --project-name \"%s\" --branch master -s \"%s\" --scan-types sast,api-security --apisec-swagger-filter \"!**/CamalCase.Schema.json\"",
                projectName, PROJECT_PATH_FOLDER);

        String includeFileCommand = String.format(
                "scan create --project-name \"%s\" --branch master -s \"%s\" --scan-types sast,api-security --apisec-swagger-filter \"!**/camalcase.Schema.json\"",
                projectName, PROJECT_PATH_FOLDER);

        try {

            Logger.info("Running CLI command (file exclusion filter): cx " + excludeFileCommand, test);
            String exclusionResult = CLIHelper.runCommand(excludeFileCommand);
            Logger.info("Scan Output (File Exclusion):\n" + exclusionResult, test);

            ScanInfo firstScanInfo = ScanUtils.extractScanInfo(exclusionResult);
            ScanUtils.validateCommonScanInfo(firstScanInfo, projectName);

            int totalWithFileExclusion = ScanUtils.extractTotalResults(exclusionResult);

            Logger.info("Total results with file exclusion: " + totalWithFileExclusion, test);

            Logger.info("Running CLI command (file inclusion due to case mismatch): cx " + includeFileCommand, test);
            String inclusionResult = CLIHelper.runCommand(includeFileCommand);
            Logger.info("Scan Output (File Inclusion):\n" + inclusionResult, test);

            int totalWithFileInclusion = ScanUtils.extractTotalResults(inclusionResult);

            Logger.info("Total results with file inclusion: " + totalWithFileInclusion, test);

            Assert.assertTrue(
                    totalWithFileInclusion > totalWithFileExclusion,
                    "Expected more results when file is included. This confirms swagger filter is case sensitive."
            );
            Logger.pass(
                    "Case sensitivity verified. Results with file inclusion (" +
                            totalWithFileInclusion +
                            ") are greater than results with file exclusion (" +
                            totalWithFileExclusion + ").",
                    test
            );
            Utils.deleteProjectById(firstScanInfo.getProjectId(), test);

        } catch (Exception e) {

            Logger.fail("Swagger filter case sensitivity test failed: " + e.getMessage(), test);
            Assert.fail("Unexpected CLI failure", e);

        }
    }

    @Test(description = "Verify CLI throws error when scan type is passed with single quotes")
    public void verifySASTScanInSingleQuotes() {

        ExtentTest test = getTestLogger();
        String projectName = "CLI_ScanProj_" + System.currentTimeMillis();

        String command = String.format(
                "scan create --project-name \"%s\" -s %s --branch \"master\" --scan-types 'sast'",
                projectName, PROJECT_PATH_ZIP
        );

        try {

            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommand(command);
            Logger.info("CLI Output:\n" + result, test);

            String expectedMessage =
                    "It looks like the \"'sast'\" scan type does not exist or you are trying to run a scan without the \"'sast'\" package license.";

            Assert.assertTrue(
                    result.contains(expectedMessage),
                    "Expected error message for invalid scan type with single quotes was not found."
            );

            Logger.pass("CLI correctly rejected scan type passed in single quotes.", test);

        } catch (Exception e) {

            Logger.fail("Single quote scan type validation test failed: " + e.getMessage(), test);
            Assert.fail("Single quote scan type validation test failed", e);

        }
    }

    @Test(description = "Verify SCA scan runs successfully with resolver")
    public void verifyScaScanWithScaResolver() {

        ExtentTest test = getTestLogger();
        String projectName = "CLI_SCAResolver_" + System.currentTimeMillis();

        String command = String.format(
                "scan create --branch master --project-name \"%s\" --scan-types sca -s %s --sca-resolver %s",
                projectName,
                PROJECT_PATH_FOLDER,
                SCA_RESOLVER_PATH
        );

        try {
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommandUntilPattern(command, OUTPUT_PATTERN, test);
            Logger.info("CLI Output:\n" + result, test);

            Assert.assertTrue(
                    result.contains("Using SCA resolver"),
                    "Expected SCA resolver execution message not found."
            );
            ScanInfo scanInfo = ScanUtils.extractScanInfo(result);
            ScanUtils.validateCommonScanInfo(scanInfo, projectName);
            Utils.deleteProjectById(scanInfo.getProjectId(), test);

        } catch (Exception e) {

            Logger.fail("SCA resolver test failed: " + e.getMessage(), test);
            Assert.fail("SCA resolver test failed", e);
        }
    }

    @Test(description = "Verify CLI throws error when invalid SCA resolver path is provided")
    public void verifyInvalidScaResolverPath() {

        ExtentTest test = getTestLogger();
        String projectName = "CLI_InvalidResolver_" + System.currentTimeMillis();

        String command = String.format(
                "scan create --branch master --project-name \"%s\" --scan-types sca -s %s --sca-resolver %s",
                projectName, PROJECT_PATH_FOLDER,
                SCA_RESOLVER_INVALID_PATH
        );
        try {
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommand(command);
            Logger.info("CLI Output:\n" + result, test);

            Assert.assertTrue(
                    result.contains("ScaResolver error: fork/exec")
                            && result.contains("The system cannot find the path specified"),
                    "Expected SCA resolver path error message not found."
            );

            Logger.pass("CLI correctly reported error for invalid SCA resolver path.", test);

        } catch (Exception e) {

            Logger.fail("Invalid SCA resolver path test failed: " + e.getMessage(), test);
            Assert.fail("Invalid SCA resolver path test failed", e);

        }
    }

    @Test(description = "Verify SCA scan runs successfully with resolver parameters")
    public void verifyScaScanWithResolverParams() {

        ExtentTest test = getTestLogger();
        String projectName = "CLI_SCAResolverParams_" + System.currentTimeMillis();
        String command = String.format(
                "scan create --branch master --project-name \"%s\" --scan-types sca -s %s --sca-resolver %s --sca-resolver-params \"--gradle-parameters='-pUSERNAME=abc -pPASSWORD=cba'\"",
                projectName,
                PROJECT_PATH_FOLDER,
                SCA_RESOLVER_PATH
        );
        try {
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommandUntilPattern(command, OUTPUT_PATTERN, test);
            Logger.info("CLI Output:\n" + result, test);

            Assert.assertTrue(
                    result.contains("Using SCA resolver"),
                    "Expected SCA resolver execution message not found."
            );
            Assert.assertTrue(
                    result.contains("--gradle-parameters=-pUSERNAME=abc -pPASSWORD=cba"),
                    "Expected resolver parameters were not passed correctly."
            );

            ScanInfo scanInfo = ScanUtils.extractScanInfo(result);
            ScanUtils.validateCommonScanInfo(scanInfo, projectName);

            Logger.pass("SCA scan executed successfully with resolver parameters.", test);
            Utils.deleteProjectById(scanInfo.getProjectId(), test);

        } catch (Exception e) {
            Logger.fail("SCA resolver params test failed: " + e.getMessage(), test);
            Assert.fail("SCA resolver params test failed", e);

        }
    }

    @Test(description = "Verify SCA scan runs successfully with multiple resolver parameters")
    public void verifyScaScanWithMultipleResolverParams() {

        ExtentTest test = getTestLogger();
        String projectName = "CLI_SCAResolverMultiParams_" + System.currentTimeMillis();

        String command = String.format(
                "scan create --branch master --project-name \"%s\" --scan-types sca -s %s --sca-resolver %s --sca-resolver-params \"--gradle-parameters='-pUSERNAME=abc -pPASSWORD=cba' --log-level Debug\"",
                projectName,
                PROJECT_PATH_FOLDER,
                SCA_RESOLVER_PATH
        );

        try {
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommandUntilPattern(command, OUTPUT_PATTERN, test);
            Logger.info("CLI Output:\n" + result, test);

            Assert.assertTrue(
                    result.contains("Using SCA resolver"),
                    "Expected SCA resolver execution message not found."
            );
            Assert.assertTrue(
                    result.contains("--gradle-parameters=-pUSERNAME=abc -pPASSWORD=cba"),
                    "Gradle parameters were not passed to resolver."
            );
            Assert.assertTrue(
                    result.contains("--log-level Debug"),
                    "Resolver log-level parameter was not passed."
            );

            ScanInfo scanInfo = ScanUtils.extractScanInfo(result);
            ScanUtils.validateCommonScanInfo(scanInfo, projectName);

            Logger.pass("SCA scan executed successfully with multiple resolver parameters.", test);
            Utils.deleteProjectById(scanInfo.getProjectId(), test);

        } catch (Exception e) {

            Logger.fail("SCA resolver multi-parameter test failed: " + e.getMessage(), test);
            Assert.fail("SCA resolver multi-parameter test failed", e);

        }
    }
}
