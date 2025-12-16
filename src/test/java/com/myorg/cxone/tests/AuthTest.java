package com.myorg.cxone.tests;

import com.aventstack.extentreports.ExtentTest;
import com.myorg.cxone.helpers.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.Base;
import utils.CLIHelper;

import static com.myorg.cxone.helpers.TestConstants.*;

public class AuthTest extends Base {

    @Test(description = "Test Auth Validation when environment variables are set")
    public void testAuthValidate() throws Exception {
        ExtentTest test = getTestLogger();

        // Run the command
        try{
            String command = "auth validate";
            String result = CLIHelper.runCommand(command);
            Logger.info("Running CLI command: cx " + command, test);
            Logger.info("CLI Output:\n" + result, test);

            // Assertion 1: No exception occurred during execution
            Assert.assertNotNull(result, DEFAULT_SUCCESS_VALIDATION_MESSAGE);

            // Assertion 2: Output contains success message
            Assert.assertTrue(
                    result.contains(SUCCESS_AUTH_VALIDATE),
                    "Expected output:\n" + SUCCESS_AUTH_VALIDATE +
                            "\n\nActual output:\n" + result
            );

            Logger.pass("Successfully authenticated to Checkmarx One server", test);
        } catch (Exception e) {
            Logger.fail("Authentication failed: " + e.getMessage(), test);
            Assert.fail("CLI Authentication failed", e);
        }
    }

    @Test(description = "Test Auth Validation when environment variables are set with empty API Key flag")
    public void testAuthValidateWhenApiKeyIsEmpty() throws Exception {
        ExtentTest test = getTestLogger();

        // Run the command
        try{
            String command = "auth validate --debug --apikey \"\"";
            String result = CLIHelper.runCommand(command);
            Logger.info("Running CLI command: cx " + command, test);
            Logger.info("CLI Output:\n" + result, test);

            Assert.assertNotNull(result, DEFAULT_SUCCESS_VALIDATION_MESSAGE);

            // Assertion 2: Output contains success message
            Assert.assertTrue(
                    result.contains(SUCCESS_AUTH_VALIDATE),
                    "Expected output:\n" + SUCCESS_AUTH_VALIDATE +
                            "\n\nActual output:\n" + result
            );
            Logger.pass("Successfully authenticated to Checkmarx One server", test);
        } catch (Exception e) {
            Logger.fail("Authentication failed: " + e.getMessage(), test);
            Assert.fail("CLI Authentication failed", e);
        }
    }

    @Test(description = "Test Auth Validation when environment variables are set with empty Client and Secret flag")
    public void testAuthValidateWhenClientSecretIsEmpty() throws Exception {
        ExtentTest test = getTestLogger();

        // Run the command
        try{
            String command = "auth validate --client-id \"\" --client-secret \"\"";
            String result = CLIHelper.runCommand(command);
            Logger.info("Running CLI command: cx " + command, test);
            Logger.info("CLI Output:\n" + result, test);

            Assert.assertNotNull(result, DEFAULT_SUCCESS_VALIDATION_MESSAGE);

            // Assertion 2: Output contains success message
            Assert.assertTrue(
                    result.contains(SUCCESS_AUTH_VALIDATE),
                    "Expected output:\n" + SUCCESS_AUTH_VALIDATE +
                            "\n\nActual output:\n" + result
            );
            Logger.pass("Successfully authenticated to Checkmarx One server", test);
        } catch (Exception e) {
            Logger.fail("Authentication failed: " + e.getMessage(), test);
            Assert.fail("CLI Authentication failed", e);
        }
    }

    @Test(description = "Test Auth Validation when environment variables are set with Invalid Api Key flag")
    public void testAuthValidateWhenApiKeyIsInvalid() throws Exception {
        ExtentTest test = getTestLogger();

        // Run the command
        try{
            String command = "auth validate --debug --apikey \"InvalidApiKey\"";
            String result = CLIHelper.runCommand(command);
            Logger.info("Running CLI command: cx " + command, test);
            Logger.info("CLI Output:\n" + result, test);

            Assert.assertNotNull(result, DEFAULT_SUCCESS_VALIDATION_MESSAGE);

            // Assertion 2: Output contains success message
            Assert.assertTrue(
                    result.contains(INVALID_API_KEY_ERROR),
                    "Expected output:\n" + INVALID_API_KEY_ERROR +
                            "\n\nActual output:\n" + result
            );
            Logger.pass("Authentication failed as expected with invalid API key", test);
        } catch (Exception e) {
            Logger.fail("Authentication failed as expected with invalid API key" + e.getMessage(), test);
            Assert.fail("Authentication failed as expected with invalid API key", e);
        }
    }

    @Test(description = "Test Auth Validation when environment variables are set with missing flags together")
    public void testAuthValidateWhenMissingFlagsTogether() throws Exception {
        ExtentTest test = getTestLogger();

        // Run the command
        try{
            String command = "auth validate --debug --client-id \"Invalid_CID\" --client-secret \"INVALID_SECRET\" --base-uri \"\" --base-auth-uri \"\" --apikey \"\"";
            String result = CLIHelper.runCommand(command);
            Logger.info("Running CLI command: cx " + command, test);
            Logger.info("CLI Output:\n" + result, test);

            Assert.assertNotNull(result, DEFAULT_SUCCESS_VALIDATION_MESSAGE);

            // Assertion 2: Output contains success message
            Assert.assertTrue(
                    result.contains(MISSING_URI),
                    "Expected output:\n" + MISSING_URI +
                            "\n\nActual output:\n" + result
            );
            Logger.pass("Authentication failed as expected with invalid command", test);
        } catch (Exception e) {
            Logger.fail("Authentication failed " + e.getMessage(), test);
            Assert.fail("Authentication failed ", e);
        }
    }

    @Test(description = "Test Auth Validation when environment variables are set with api key and Client secret flag is empty")
    public void testAuthValidateWhenClientIdAndApiKeyAreEmpty() throws Exception {
        ExtentTest test = getTestLogger();

        // Run the command
        try{
            String command = "auth validate --debug --client-id \"\" --apikey \"\"";
            String result = CLIHelper.runCommand(command);
            Logger.info("Running CLI command: cx " + command, test);
            Logger.info("CLI Output:\n" + result, test);

            Assert.assertNotNull(result, DEFAULT_SUCCESS_VALIDATION_MESSAGE);

            // Assertion 2: Output contains success message
            Assert.assertTrue(
                    result.contains(FAILED_AUTHENTICATION),
                    "Expected output:\n" + FAILED_AUTHENTICATION +
                            "\n\nActual output:\n" + result
            );
            Logger.pass("Authentication failed as expected with invalid command", test);
        } catch (Exception e) {
            Logger.fail("Authentication failed " + e.getMessage(), test);
            Assert.fail("Authentication failed ", e);
        }
    }
}
