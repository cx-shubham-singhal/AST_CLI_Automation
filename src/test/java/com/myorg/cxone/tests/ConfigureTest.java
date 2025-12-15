package com.myorg.cxone.tests;

import com.aventstack.extentreports.ExtentTest;
import utils.CLIHelper;
import com.myorg.cxone.helpers.Logger;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import utils.Base;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.myorg.cxone.helpers.TestConstants.*;

public class ConfigureTest extends Base {

    @Test(description = "Test cx configure displays setup guide on start")
    public void testConfigureDisplaysSetupGuide() {
        ExtentTest test = getTestLogger();

        try {
            String command = "configure";
            String result = CLIHelper.runCommandWithTimeout(command, 3000); // Wait 3 seconds for output

            Logger.info("Running CLI command: cx " + command, test);
            Logger.info("CLI Output:\n" + result, test);

            Assert.assertNotNull(result, "CLI should return some output");

            Assert.assertTrue(
                    result.contains(SETUP_GUIDE),
                    "Expected output:\n" + SETUP_GUIDE +
                            "\n\nActual output:\n" + result
            );

            Logger.pass("Setup guide displayed correctly at the start of `cx configure`", test);

        } catch (Exception e) {
            Logger.fail("cx configure failed: " + e.getMessage(), test);
            Assert.fail("cx configure failed", e);
        }
    }

    @Test(description = "Test configure set invalid property command")
    public void testConfigureSetInvalidProperty() {
        ExtentTest test = getTestLogger();

        try {
            String propName = "InvalidPropertyName";
            String propValue = "InvalidPropertyValue";
            String command = String.format("configure set --prop-name %s --prop-value \"%s\"", propName, propValue);

            String result = CLIHelper.runCommand(command);
            Logger.info("Running CLI command: cx " + command, test);
            Logger.info("CLI Output:\n" + result, test);

            Assert.assertNotNull(result, "CLI should return some output");
            Assert.assertTrue(result.contains(INVALID_STATE_ERROR),
                    "Expected error output to contain: \n" + INVALID_STATE_ERROR + "\n\nActual output:\n" + result);

            Logger.pass("Invalid property command failed as expected", test);
        } catch (Exception e) {
            Logger.fail("configure set command (invalid) threw an exception: " + e.getMessage(), test);
            Assert.fail("CLI configure set failed unexpectedly", e);
        }
    }

    @Test(description = "Test configure set with empty property name and value")
    public void testConfigureSetEmptyPropertyNameAndValue() {
        ExtentTest test = getTestLogger();

        try {
            String propName = "";
            String propValue = "";
            String command = String.format("configure set --prop-name \"%s\" --prop-value \"%s\"", propName, propValue);

            String result = CLIHelper.runCommand(command);
            Logger.info("Running CLI command: cx " + command, test);
            Logger.info("CLI Output:\n" + result, test);

            Assert.assertNotNull(result, "CLI should return some output");

            // Expecting an error message because both key and value are invalid/empty
            String expectedError = "Failed to set property";
            Assert.assertTrue(result.toLowerCase().contains(expectedError.toLowerCase()),
                    "Expected error message not found.\nExpected to contain: " + expectedError + "\nActual output:\n" + result);

            Logger.pass("CLI correctly failed to set with empty key and value", test);

        } catch (Exception e) {
            Logger.fail("configure set command with empty key/value threw exception: " + e.getMessage(), test);
            Assert.fail("Unexpected exception for empty key/value CLI test", e);
        }
    }


    @Test(description = "Test configure set command with valid cx_apikey")
    public void testConfigureSetApiKeyProperty() {
        ExtentTest test = getTestLogger();

        try {
            // Load API key from environment
            String propName = "cx_apikey";
            String propValue = System.getenv("CX_APIKEY");

            Assert.assertNotNull(propValue, "Environment variable CX_APIKEY must be set.");

            String command = String.format("configure set --prop-name %s --prop-value \"%s\"", propName, propValue);
            String result = CLIHelper.runCommand(command);

            Logger.info("Running CLI command: cx " + command, test);
            Logger.info("CLI Output:\n" + result, test);

            Assert.assertNotNull(result, "CLI should return some output");

            String expectedPrefix = "Setting property [ " + propName + " ] to value [ ";
            Assert.assertTrue(result.contains(expectedPrefix),
                    "Expected output should contain prefix: \n" + expectedPrefix + "\n\nActual output:\n" + result);

            Logger.pass("API key property set successfully", test);

        } catch (Exception e) {
            Logger.fail("configure set for API key failed: " + e.getMessage(), test);
            Assert.fail("CLI configure set command failed", e);
        }
    }

    @Test(description = "Test configure set with empty cx_apikey (invalid)")
    public void testConfigureSetEmptyApiKey() {
        ExtentTest test = getTestLogger();

        try {
            String propName = "cx_apikey";
            String emptyValue = ""; // Intentionally empty

            String command = String.format("configure set --prop-name %s --prop-value \"%s\"", propName, emptyValue);
            String result = CLIHelper.runCommand(command);

            Logger.info("Running CLI command: cx " + command, test);
            Logger.info("CLI Output:\n" + result, test);

            Assert.assertNotNull(result, "CLI should return some output");

            // Expected error message from CLI
            String expectedPrefix = "Setting property [ " + propName + " ] to value [ ";
            Assert.assertTrue(result.contains(expectedPrefix),
                    "Expected output should contain prefix: \n" + expectedPrefix + "\n\nActual output:\n" + result);

            Logger.pass("API key property set successfully as empty", test);

        } catch (Exception e) {
            Logger.fail("Test failed with exception: " + e.getMessage(), test);
            Assert.fail("Unexpected exception in CLI test for empty API key", e);
        }
    }

    @Test(description = "Test configure set cx_base_uri")
    public void testConfigureSetBaseUriProperty() {
        ExtentTest test = getTestLogger();

        try {
            String propName = "cx_base_uri";
            String propValue = System.getenv("CX_BASE_URI");
            Assert.assertNotNull(propValue, "Environment variable CX_BASE_URI must be set.");

            String command = String.format("configure set --prop-name %s --prop-value \"%s\"", propName, propValue);
            String result = CLIHelper.runCommand(command);

            Logger.info("Running CLI command: cx " + command, test);
            Logger.info("CLI Output:\n" + result, test);

            Assert.assertNotNull(result, "CLI should return some output");

            String expectedPrefix = "Setting property [ " + propName + " ] to value [ ";
            Assert.assertTrue(result.contains(expectedPrefix),
                    "Expected output should contain prefix: \n" + expectedPrefix + "\n\nActual output:\n" + result);

            Logger.pass("BASE URI Value set successfully", test);

        } catch (Exception e) {
            Logger.fail("configure set for BASE URI failed: " + e.getMessage(), test);
            Assert.fail("CLI configure set command failed", e);
        }
    }

    @Test(description = "Test configure set cx_base_uri")
    public void testConfigureSetBaseAuthUriProperty() {
        ExtentTest test = getTestLogger();

        try {
            String propName = "cx_base_auth_uri";
            String propValue = System.getenv("CX_BASE_AUTH_URI");
            Assert.assertNotNull(propValue, "Environment variable CX_BASE_AUTH_URI must be set.");

            String command = String.format("configure set --prop-name %s --prop-value \"%s\"", propName, propValue);
            String result = CLIHelper.runCommand(command);

            Logger.info("Running CLI command: cx " + command, test);
            Logger.info("CLI Output:\n" + result, test);

            Assert.assertNotNull(result, "CLI should return some output");

            String expectedPrefix = "Setting property [ " + propName + " ] to value [ ";
            Assert.assertTrue(result.contains(expectedPrefix),
                    "Expected output should contain prefix: \n" + expectedPrefix + "\n\nActual output:\n" + result);

            Logger.pass("BASE AUTH URI Value set successfully", test);

        } catch (Exception e) {
            Logger.fail("configure set for BASE AUTH URI failed: " + e.getMessage(), test);
            Assert.fail("CLI configure set command failed", e);
        }
    }

    @Test(description = "Test configure set cx_base_uri")
    public void testConfigureSetTenantProperty() {
        ExtentTest test = getTestLogger();

        try {
            String propName = "cx_tenant";
            String propValue = System.getenv("CX_TENANT");
            Assert.assertNotNull(propValue, "Environment variable CX_TENANT must be set.");

            String command = String.format("configure set --prop-name %s --prop-value \"%s\"", propName, propValue);
            String result = CLIHelper.runCommand(command);

            Logger.info("Running CLI command: cx " + command, test);
            Logger.info("CLI Output:\n" + result, test);

            Assert.assertNotNull(result, "CLI should return some output");

            String expectedPrefix = "Setting property [ " + propName + " ] to value [ ";
            Assert.assertTrue(result.contains(expectedPrefix),
                    "Expected output should contain prefix: \n" + expectedPrefix + "\n\nActual output:\n" + result);

            Logger.pass("TENANT Value set successfully", test);

        } catch (Exception e) {
            Logger.fail("configure set for tenant failed: " + e.getMessage(), test);
            Assert.fail("CLI configure set command failed", e);
        }
    }

    @Test(description = "Test configure set cx_base_uri")
    public void testConfigureSetHttpProxyProperty() {
        ExtentTest test = getTestLogger();

        try {
            String propName = "http_proxy";
            String propValue = "";

            String command = String.format("configure set --prop-name %s --prop-value \"%s\"", propName, propValue);
            String result = CLIHelper.runCommand(command);

            Logger.info("Running CLI command: cx " + command, test);
            Logger.info("CLI Output:\n" + result, test);

            Assert.assertNotNull(result, "CLI should return some output");

            String expectedPrefix = "Setting property [ " + propName + " ] to value [ ";
            Assert.assertTrue(result.contains(expectedPrefix),
                    "Expected output should contain prefix: \n" + expectedPrefix + "\n\nActual output:\n" + result);

            Logger.pass("Http Proxy set successfully", test);

        } catch (Exception e) {
            Logger.fail("configure set for http proxy failed: " + e.getMessage(), test);
            Assert.fail("CLI configure set command failed", e);
        }
    }

    @Test(description = "Test configure set cx_client_id property")
    public void testConfigureSetClientIdProperty() {
        ExtentTest test = getTestLogger();

        try {
            String propName = "cx_client_id";
            String propValue = System.getenv("CX_CLIENT_ID");

            Assert.assertNotNull(propValue, "Environment variable CX_CLIENT_ID must be set.");

            String command = String.format("configure set --prop-name %s --prop-value \"%s\"", propName, propValue);
            String result = CLIHelper.runCommand(command);

            Logger.info("Running CLI command: cx " + command, test);
            Logger.info("CLI Output:\n" + result, test);

            Assert.assertNotNull(result, "CLI should return some output");

            String expectedPrefix = "Setting property [ " + propName + " ] to value [ ";
            Assert.assertTrue(result.contains(expectedPrefix),
                    "Expected output should contain prefix: \n" + expectedPrefix + "\n\nActual output:\n" + result);

            Logger.pass("Client ID property set successfully", test);

        } catch (Exception e) {
            Logger.fail("configure set for cx_client_id failed: " + e.getMessage(), test);
            Assert.fail("CLI configure set command failed", e);
        }
    }

    @Test(description = "Test configure set cx_client_secret property")
    public void testConfigureSetClientSecretProperty() {
        ExtentTest test = getTestLogger();

        try {
            String propName = "cx_client_secret";
            String propValue = System.getenv("CX_CLIENT_SECRET");

            Assert.assertNotNull(propValue, "Environment variable CX_CLIENT_SECRET must be set.");

            String command = String.format("configure set --prop-name %s --prop-value \"%s\"", propName, propValue);
            String result = CLIHelper.runCommand(command);

            Logger.info("Running CLI command: cx " + command, test);
            Logger.info("CLI Output:\n" + result, test);

            Assert.assertNotNull(result, "CLI should return some output");

            String expectedPrefix = "Setting property [ " + propName + " ] to value [ ";
            Assert.assertTrue(result.contains(expectedPrefix),
                    "Expected output should contain prefix: \n" + expectedPrefix + "\n\nActual output:\n" + result);

            Logger.pass("Client secret property set successfully", test);

        } catch (Exception e) {
            Logger.fail("configure set for cx_client_secret failed: " + e.getMessage(), test);
            Assert.fail("CLI configure set command failed", e);
        }
    }

    @Test(description = "Test `cx configure show` displays effective configuration")
    public void testConfigureShowDisplaysEffectiveConfiguration() {
        ExtentTest test = getTestLogger();

        try {
            String command = "configure show";
            String result = CLIHelper.runCommand(command);

            Logger.info("Running CLI command: cx " + command, test);
            Logger.info("CLI Output:\n" + result, test);

            Assert.assertNotNull(result, "CLI should return some output");

            // List of expected configuration lines
            List<String> expectedKeys = Arrays.asList(
                    "Current Effective Configuration",
                    "BaseURI:",
                    "BaseAuthURIKey:",
                    "Checkmarx One Tenant:",
                    "Client ID:",
                    "Client Secret:",
                    "APIKey:",
                    "Proxy:"
            );

            Optional<String> missingKey = expectedKeys.stream()
                    .filter(key -> !result.contains(key))
                    .findFirst();

            if (missingKey.isPresent()) {
                Logger.fail("Missing expected key: " + missingKey.get(), test);
                Assert.fail("Expected key not found in output: " + missingKey.get() +
                        "\n\nActual Output:\n" + result);
            }

            Logger.pass("All expected configuration fields displayed correctly", test);

        } catch (Exception e) {
            Logger.fail("configure set for cx_client_secret failed: " + e.getMessage(), test);
            Assert.fail("CLI configure set command failed", e);
        }

    }
}
