package com.myorg.cxone.tests;

import com.aventstack.extentreports.ExtentTest;
import utils.CLIHelper;
import com.myorg.cxone.helpers.Logger;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import utils.Base;
import utils.ProjectUtils;
import utils.Utils;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Listeners(utils.ExcelReportListener.class)
public class ProjectTest extends Base {

    @Test(description = "Create Test Project")
    public void createProjectTest() {
        ExtentTest test = getTestLogger();

        String projectName = "CLI_AutomationProj_" + System.currentTimeMillis();
        String command = String.format("project create --project-name \"%s\"", projectName);

        try {
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommand(command);
            Logger.info("CLI Output:\n" + result, test);

            boolean isSuccess = result.toLowerCase().contains("created")
                    || result.toLowerCase().contains("success")
                    || result.toLowerCase().contains(projectName.toLowerCase());
            String projectId = extractProjectId(result);
            Assert.assertNotNull(projectId, "Failed to extract Project ID from CLI output.");
            Logger.info("Extracted Project ID: " + projectId, test);

            String showOutput = ProjectUtils.showProjectById(projectId,"", test);
            Assert.assertTrue(showOutput.contains(projectId), "Show command did not return expected project!");

            Assert.assertTrue(
                    isSuccess,
                    "Project creation output did not contain expected confirmation.\n\nExpected project name: " +
                            projectName + "\nCLI Output:\n" + result
            );

            Logger.info("Project '" + projectName + "' created successfully.", test);
            Utils.deleteProjectById(projectId, test);

        } catch (Exception e) {
            Logger.fail("Project creation test failed: " + e.getMessage(), test);
            Assert.fail("CLI project creation failed", e);
        }
    }

    @Test(description = "Verify project creation fails when project name is empty")
    public void testCreateProjectWithEmptyName() {
        ExtentTest test = getTestLogger();

        String command = "project create --project-name \"\"";  // Empty project name

        try {
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommand(command);
            Logger.info("CLI Output:\n" + result, test);
            
            boolean containsError = result.toLowerCase().contains("project name is required");

            Assert.assertTrue(
                    containsError,
                    "Expected error message was not displayed.\nExpected: 'project name is required'\nActual Output:\n" + result
            );

            Logger.pass("Proper validation message displayed for empty project name.", test);

        } catch (Exception e) {
            Logger.fail("Test failed while validating empty project name: " + e.getMessage(), test);
            Assert.fail("CLI command failed unexpectedly", e);
        }
    }

    @Test(description = "Create project with valid application name")
    public void testCreateProjectWithValidApplication() {
        ExtentTest test = getTestLogger();
        String projectName = "CLI_AutomationProj_" + System.currentTimeMillis();
        String appName = "ShubhamApplicationForAutomation";
        String command = String.format("project create --project-name \"%s\" --application-name \"%s\"", projectName, appName);

        try {
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommand(command);
            Logger.info("CLI Output:\n" + result, test);

            String projectId = extractProjectId(result);
            Assert.assertNotNull(projectId, "Failed to extract Project ID from CLI output.");
            Logger.info("Extracted Project ID: " + projectId, test);

            String showOutput = ProjectUtils.showProjectById(projectId,"", test);
            Assert.assertTrue(showOutput.contains(projectId), "Show command did not return expected project!");

            String applicationId = extractApplicationId(result);
            Assert.assertNotNull(applicationId, "Failed to extract Application ID from CLI output.");

            Assert.assertTrue(showOutput.contains(applicationId),
                    "Application ID was not mapped correctly in show output!\nExpected ID: " + applicationId + "\nOutput:\n" + showOutput);

            Logger.pass("Project with valid application created successfully.", test);
            Utils.deleteProjectById(projectId, test);
        } catch (Exception e) {
            Logger.fail("Test failed while creating project with valid application: " + e.getMessage(), test);
            Assert.fail("CLI execution failed.", e);
        }
    }

    @Test(description = "Fail to create project with invalid application name")
    public void testCreateProjectWithInvalidApplication() {
        ExtentTest test = getTestLogger();
        String projectName = "CLI_AutomationProj_" + System.currentTimeMillis();
        String invalidAppName = "INVALID_APPLICATION";
        String command = String.format("project create --project-name \"%s\" --application-name \"%s\"", projectName, invalidAppName);

        try {
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommand(command);
            Logger.info("CLI Output:\n" + result, test);

            Assert.assertTrue(
                    result.toLowerCase().contains("provided application does not exist")
                            || result.toLowerCase().contains("no permission"),
                    "Expected failure message for invalid application was not displayed.\nOutput:\n" + result
            );

            Logger.pass("Proper error shown when using invalid application.", test);

        } catch (Exception e) {
            Logger.fail("Unexpected failure while testing invalid application case: " + e.getMessage(), test);
            Assert.fail("CLI failed unexpectedly.", e);
        }
    }


    @Test(description = "Create project with empty application name - should succeed without binding")
    public void testCreateProjectWithEmptyApplication() {
        ExtentTest test = getTestLogger();
        String projectName = "CLI_AutomationProj_" + System.currentTimeMillis();
        String command = String.format("project create --project-name \"%s\" --application-name \"\"", projectName);

        try {
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommand(command);
            Logger.info("CLI Output:\n" + result, test);

            String projectId = extractProjectId(result);
            Assert.assertNotNull(projectId, "Failed to extract Project ID from CLI output.");
            Logger.info("Extracted Project ID: " + projectId, test);

            String showOutput = ProjectUtils.showProjectById(projectId,"", test);
            Assert.assertTrue(showOutput.contains(projectId), "Show command did not return expected project!");
            Assert.assertTrue(showOutput.contains("[]"), "ApplicationIds should be empty when no app is mapped!");

            Logger.pass("Project created successfully without application mapping.", test);
            Utils.deleteProjectById(projectId, test);

        } catch (Exception e) {
            Logger.fail("Test failed while creating project without application: " + e.getMessage(), test);
            Assert.fail("CLI execution failed.", e);
        }
    }

    @Test(description = "Verify duplicate project creation fails with proper error")
    public void createDuplicateProjectTest() {
        ExtentTest test = getTestLogger();
        String projectName = "duplicateProject_" + System.currentTimeMillis();

        try {
            // First Creation - Should Pass
            Logger.info("Creating initial project: " + projectName, test);
            String result1 = CLIHelper.runCommand(String.format("project create --project-name \"%s\"", projectName));
            Logger.info("CLI Output (First Create):\n" + result1, test);
            String projectId = extractProjectId(result1);
            Assert.assertNotNull(projectId, "Failed to extract Project ID from first creation.");

            // Second Creation - Should Fail
            Logger.info("Attempting to create duplicate project: " + projectName, test);
            String result2 = CLIHelper.runCommand(String.format("project create --project-name \"%s\"", projectName));
            Logger.info("CLI Output (Duplicate Create):\n" + result2, test);

            boolean hasDuplicateError = result2.toLowerCase().contains("already exists")
                    || result2.toLowerCase().contains("code: 208")
                    || result2.toLowerCase().contains("failed creating a project");

            Assert.assertTrue(hasDuplicateError,
                    "Duplicate project creation did not return expected error!\nCLI Output:\n" + result2);

            Logger.pass("Duplicate project creation correctly rejected.", test);

            // Cleanup original project
            Utils.deleteProjectById(projectId, test);

        } catch (Exception e) {
            Logger.fail("Duplicate project test failed: " + e.getMessage(), test);
            Assert.fail("Duplicate project test failed", e);
        }
    }

    @Test(description = "List Projects with limit=10")
    public void listProjectsWithLimitTest() {
        ExtentTest test = getTestLogger();

        int limit = 10; // ✅ Change this value to test different limits
        String command = String.format("project list --filter \"limit=%d\"", limit);

        try {
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommand(command);
            Logger.info("CLI Output:\n" + result, test);

            // Basic check: Output should contain the table headers and at least one project row
            boolean hasHeaders = result.toLowerCase().contains("project id") && result.toLowerCase().contains("name");
            boolean hasRows = result.split("\n").length > 2; // 2 lines are header lines

            Assert.assertTrue(hasHeaders, "Project list output is missing table headers.");
            Assert.assertTrue(hasRows, "Project list output does not contain any project rows.");

            // Optional: Check that number of project rows <= 10
            int rowCount = result.split("\n").length - 3; // subtract header lines
            Assert.assertTrue(rowCount <= limit,
                    "Project list returned more than " + limit + " projects. Count: " + rowCount);

            Logger.pass("Project list command executed successfully with limit=" + limit, test);

        } catch (Exception e) {
            Logger.fail("Project list test failed: " + e.getMessage(), test);
            Assert.fail("CLI project list failed", e);
        }
    }

    @Test(description = "List Projects with limit=5 and offset=10")
    public void listProjectsWithLimitAndOffsetTest() {
        ExtentTest test = getTestLogger();

        int limit = 5;
        int offset = 10;
        String command = String.format("project list --filter \"limit=%d,offset=%d\"", limit, offset);

        try {
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommand(command);
            Logger.info("CLI Output:\n" + result, test);

            // Basic check: Output should contain the table headers and at least one project row
            boolean hasHeaders = result.toLowerCase().contains("project id") && result.toLowerCase().contains("name");
            boolean hasRows = result.split("\n").length > 2; // 2 lines are header lines

            Assert.assertTrue(hasHeaders, "Project list output is missing table headers.");
            Assert.assertTrue(hasRows, "Project list output does not contain any project rows.");

            // Check number of project rows <= limit
            int rowCount = result.split("\n").length - 3; // subtract header lines
            Assert.assertTrue(rowCount <= limit,
                    "Project list returned more than " + limit + " projects. Count: " + rowCount);

            Logger.pass("Project list command executed successfully with limit=" + limit + " and offset=" + offset, test);

        } catch (Exception e) {
            Logger.fail("Project list with limit and offset test failed: " + e.getMessage(), test);
            Assert.fail("CLI project list failed", e);
        }
    }


    @Test(description = "List Projects with JSON output and dynamic limit")
    public void listProjectsWithJsonFlagTest() {
        ExtentTest test = getTestLogger();

        int limit = 10; // dynamic limit
        String command = String.format("project list --format json --filter \"limit=%d\"", limit);

        try {
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommand(command);
            List<Map<String, Object>> projects = ProjectUtils.parseProjectsJson(result,test);

            Assert.assertTrue(projects.size() <= limit,
                    "Project list returned more than " + limit + " projects. Count: " + projects.size());

        } catch (Exception e) {
            Logger.fail("Project list test failed: " + e.getMessage(), test);
            Assert.fail("CLI project list failed", e);
        }
    }

    @Test(description = "List Projects filtered by specific IDs")
    public void listProjectsByIdsTest() {
        ExtentTest test = getTestLogger();

        try {
            // 1️⃣ Create first project
            String projectName1 = "filterById_" + System.currentTimeMillis();
            String createCommand1 = String.format("project create --project-name \"%s\"", projectName1);
            String result1 = CLIHelper.runCommand(createCommand1);
            String projectId1 = extractProjectId(result1);

            // 2️⃣ Create second project
            String projectName2 = "filterById1_" + System.currentTimeMillis();
            String createCommand2 = String.format("project create --project-name \"%s\"", projectName2);
            Logger.info("Running CLI command: cx " + createCommand2, test);
            String result2 = CLIHelper.runCommand(createCommand2);
            String projectId2 = extractProjectId(result2);


            // 3️⃣ List projects using the IDs filter
            String listCommand = String.format("project list --filter \"ids=%s;%s\"", projectId1, projectId2);
            Logger.info("Running CLI command: cx " + listCommand, test);
            String listResult = CLIHelper.runCommand(listCommand);
            Logger.info("CLI Output:\n" + listResult, test);

            // 4️⃣ Assertions: Output must contain both project IDs
            Assert.assertTrue(listResult.contains(projectId1), "First project ID not found in list output!");
            Assert.assertTrue(listResult.contains(projectId2), "Second project ID not found in list output!");
            Logger.pass("Project list by IDs executed successfully. Both projects are present.", test);

            // 5️⃣ Cleanup: Delete created projects
            ProjectUtils.deleteProjectById(projectId1, test);
            ProjectUtils.deleteProjectById(projectId2, test);

        } catch (Exception e) {
            Logger.fail("List projects by IDs test failed: " + e.getMessage(), test);
            Assert.fail("CLI project list by IDs failed", e);
        }
    }

    @Test(description = "Create Project with Tags and Verify")
    public void createProjectWithTagsTest() {
        ExtentTest test = getTestLogger();

        try {
            String projectName = "TagProjectAutomation_" + System.currentTimeMillis();
            String tagKey = "Environment";
            String tagValue = "QA_Automation";
            String tagFull = tagKey + ":" + tagValue;

            String createCommand = String.format("project create --project-name \"%s\" --tags \"%s\"", projectName, tagFull);
            Logger.info("Running CLI command: cx " + createCommand, test);

            String result = CLIHelper.runCommand(createCommand);
            Logger.info("CLI Output:\n" + result, test);

            String projectId = extractProjectId(result);
            Assert.assertNotNull(projectId, "Failed to extract Project ID!");

            Logger.info("Project created with ID: " + projectId, test);

            Assert.assertTrue(result.contains(tagFull),
                    "Tag '" + tagFull + "' was not found in the project creation output!");

            Logger.pass("Project created successfully with correct tag: " + tagFull, test);

            Utils.deleteProjectById(projectId, test);

        } catch (Exception e) {
            Logger.fail("Create project with tags test failed: " + e.getMessage(), test);
            Assert.fail("CLI project create with tags failed", e);
        }
    }

    @Test(description = "List Projects by Tags and Log if None Found")
    public void listProjectsByTagsTest() {
        ExtentTest test = getTestLogger();

        String tagKey = "Environment";
        String tagValue = "QA_Automation";
        String filterCommand = String.format("project list --filter \"tags-keys=%s,tags-values=%s\"", tagKey, tagValue);

        try {
            Logger.info("Running CLI command: cx " + filterCommand, test);
            String result = CLIHelper.runCommand(filterCommand);
            Logger.info("CLI Output:\n" + result, test);

            // Normalize response (handle case where output might be blank)
            String trimmedResult = result.trim();

            if (trimmedResult.isEmpty()) {
                Logger.info("No projects found with tag " + tagKey + ":" + tagValue, test);
            } else {
                // Basic validation: Output contains at least one Project ID and Tag
                boolean hasProjectId = trimmedResult.toLowerCase().contains("project id");
                boolean hasTag = trimmedResult.contains(tagKey + ":" + tagValue);

                Assert.assertTrue(hasProjectId, "Expected project list output with headers, but got unexpected response.");
                Assert.assertTrue(hasTag, "Expected tag " + tagKey + ":" + tagValue + " not found in listed projects.");

                Logger.pass("Projects with tag " + tagKey + ":" + tagValue + " found successfully.", test);
            }

        } catch (Exception e) {
            Logger.fail("Project list by tags test failed: " + e.getMessage(), test);
            Assert.fail("CLI project list by tags failed", e);
        }
    }

    @Test
    public void listProjectTags() {
        ExtentTest test = getTestLogger();

        try {
            String command = "project tags";
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommand(command); // returns JSON

            Map<String, List<String>> tags = ProjectUtils.parseTagsJson(result, test);

            // Optional assertion example
            Assert.assertTrue(tags.size() > 0, "No tags returned from project tags!");
        } catch (Exception e) {
            Logger.fail("Project list by tags test failed: " + e.getMessage(), test);
            Assert.fail("CLI project list by tags failed", e);
        }
    }

    @Test(description = "Verify project show command in table format")
    public void showProjectInTableFormatTest() {
        ExtentTest test = getTestLogger();
        String projectName = "CLI_AutomationProj_" + System.currentTimeMillis();
        String command = String.format("project create --project-name \"%s\"", projectName);

        try {
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommand(command);
            Logger.info("CLI Output:\n" + result, test);

            String projectId = extractProjectId(result);
            Assert.assertNotNull(projectId, "Failed to extract Project ID from CLI output.");
            Logger.info("Extracted Project ID: " + projectId, test);

            String tableOutput = ProjectUtils.showProjectById(projectId, "table", test);
            String tableFormat = ProjectUtils.detectOutputFormat(tableOutput, test);
            Assert.assertEquals(tableFormat, "table", "Expected TABLE format output.");
            Logger.pass("Verified TABLE format successfully.", test);

            Utils.deleteProjectById(projectId, test);

        } catch (Exception e) {
            Logger.fail("Test failed: " + e.getMessage(), test);
            Assert.fail("Show project format validation failed", e);
        }
    }

    @Test(description = "Verify project show command in Json format")
    public void showProjectInTableJsonTest() {
        ExtentTest test = getTestLogger();
        String projectName = "CLI_AutomationProj_" + System.currentTimeMillis();
        String command = String.format("project create --project-name \"%s\"", projectName);

        try {
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommand(command);
            Logger.info("CLI Output:\n" + result, test);

            String projectId = extractProjectId(result);
            Assert.assertNotNull(projectId, "Failed to extract Project ID from CLI output.");
            Logger.info("Extracted Project ID: " + projectId, test);

            String jsonOutput = ProjectUtils.showProjectById(projectId, "json", test);
            String jsonFormat = ProjectUtils.detectOutputFormat(jsonOutput, test);
            Assert.assertEquals(jsonFormat, "json", "Expected JSON format output.");
            Logger.pass("Verified JSON format successfully.", test);

            Utils.deleteProjectById(projectId, test);

        } catch (Exception e) {
            Logger.fail("Test failed: " + e.getMessage(), test);
            Assert.fail("Show project format validation failed", e);
        }
    }
    @Test(description = "Verify project show command in list format")
    public void showProjectInListFormatTest() {
        ExtentTest test = getTestLogger();
        String projectName = "CLI_AutomationProj_" + System.currentTimeMillis();
        String command = String.format("project create --project-name \"%s\"", projectName);

        try {
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommand(command);
            Logger.info("CLI Output:\n" + result, test);

            String projectId = extractProjectId(result);
            Assert.assertNotNull(projectId, "Failed to extract Project ID from CLI output.");
            Logger.info("Extracted Project ID: " + projectId, test);

            String listOutput = ProjectUtils.showProjectById(projectId, "list", test);
            String listFormat = ProjectUtils.detectOutputFormat(listOutput, test);
            Assert.assertEquals(listFormat, "list", "Expected LIST format output.");
            Logger.pass("Verified LIST format successfully.", test);

            Utils.deleteProjectById(projectId, test);

        } catch (Exception e) {
            Logger.fail("Test failed: " + e.getMessage(), test);
            Assert.fail("Show project format validation failed", e);
        }
    }

    @Test(description = "Delete Project with Valid Project ID")
    public void deleteProjectWithValidIdTest() {
        ExtentTest test = getTestLogger();

        String projectName = "CLI_AutomationProj_" + System.currentTimeMillis();
        String command = String.format("project create --project-name \"%s\"", projectName);

        try {
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommand(command);
            Logger.info("CLI Output:\n" + result, test);

            String projectId = extractProjectId(result);
            Assert.assertNotNull(projectId, "Failed to extract Project ID from CLI output.");
            Logger.info("Extracted Project ID: " + projectId, test);

            Utils.deleteProjectById(projectId, test);

        } catch (Exception e) {
            Logger.fail("Project deletion test failed: " + e.getMessage(), test);
            Assert.fail("CLI project deletion test failed", e);
        }
    }

    @Test(description = "Delete Project with Invalid Project ID")
    public void deleteProjectWithInvalidIdTest() {
        ExtentTest test = getTestLogger();

        String invalidProjectId = "79b545da-79f9-49f8-968d-e6089c9e7100";
        String command = String.format("project delete --project-id %s", invalidProjectId);

        try {
            Logger.info("Running CLI command: cx " + command, test);
            String result = CLIHelper.runCommand(command);
            Logger.info("CLI Output:\n" + result, test);

            // Assert: 404 error should appear in the CLI output
            boolean contains404 = result.toLowerCase().contains("404") ||
                    result.toLowerCase().contains("failed deleting a project");

            Assert.assertTrue(contains404,
                    "Expected 404 error when deleting invalid project ID, but got:\n" + result);

            Logger.pass("CLI correctly returned 404 for invalid project ID: " + invalidProjectId, test);

        } catch (Exception e) {
            Logger.fail("Invalid project deletion test failed: " + e.getMessage(), test);
            Assert.fail("CLI invalid project deletion test failed", e);
        }
    }

    private String extractProjectId(String cliOutput) {
        // Regex to match a UUID
        Matcher matcher = Pattern.compile("[a-fA-F0-9\\-]{36}").matcher(cliOutput);
        return matcher.find() ? matcher.group() : null;
    }

    private String extractApplicationId(String cliOutput) {
        Pattern pattern = Pattern.compile("\\[([a-f0-9\\-]+)\\]");  // extracts [UUID]
        Matcher matcher = pattern.matcher(cliOutput);
        if (matcher.find()) {
            return matcher.group(1); // return UUID without brackets
        }
        return null;
    }
}