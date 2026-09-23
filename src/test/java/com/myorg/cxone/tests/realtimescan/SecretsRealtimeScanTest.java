package com.myorg.cxone.tests.realtimescan;

import com.aventstack.extentreports.ExtentTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.myorg.cxone.helpers.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.Base;
import utils.RealtimeScanUtils;

import static com.myorg.cxone.helpers.TestConstants.SECRETS_REALTIME_FILE_PATH;

public class SecretsRealtimeScanTest extends Base {

    @Test(description = "Verify Secrets realtime scan runs successfully and detects exposed secrets in a file")
    public void verifySecretsRealtimeScanDetectsSecrets() throws Exception {
        ExtentTest test = getTestLogger();
        String command = String.format("scan secrets-realtime -s %s", SECRETS_REALTIME_FILE_PATH);

        try {
            JsonNode root = RealtimeScanUtils.runRealtimeScanCommand(command, test);

            Assert.assertTrue(root.isArray() && !root.isEmpty(),
                    "Expected Secrets realtime scan to detect at least one secret.\nCLI Output:\n" + root);

            int secretsCount = root.size();
            Assert.assertTrue(secretsCount >= 1,
                    "Expected Secrets realtime scan to detect at least one secret.\nCLI Output:\n" + root);
            RealtimeScanUtils.logVulnerabilityCount(secretsCount, test);
            Logger.pass("Secrets realtime scan completed successfully and detected " + secretsCount + " secret(s)", test);
        } catch (Exception e) {
            Logger.fail("Secrets realtime scan test failed: " + e.getMessage(), test);
            Assert.fail("Secrets realtime scan test failed", e);
        }
    }
}
