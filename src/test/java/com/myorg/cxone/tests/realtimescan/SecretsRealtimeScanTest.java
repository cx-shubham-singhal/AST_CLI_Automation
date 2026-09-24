package com.myorg.cxone.tests.realtimescan;

import com.aventstack.extentreports.ExtentTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.myorg.cxone.helpers.Logger;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import utils.Base;
import utils.RealtimeScanUtils;

import static com.myorg.cxone.helpers.TestConstants.*;

public class SecretsRealtimeScanTest extends Base {

    @DataProvider(name = "secretsSourceFiles")
    public Object[][] secretsSourceFiles() {
        return new Object[][]{
                {"python (secrets.py)", SECRETS_REALTIME_FILE_PATH},
                {"java (application.properties)", SECRETS_REALTIME_APPLICATION_PROPERTIES_FILE_PATH},
                {"dotnet (appsettings.json)", SECRETS_REALTIME_APPSETTINGS_JSON_FILE_PATH},
                {"javascript (config.js)", SECRETS_REALTIME_CONFIG_JS_FILE_PATH},
                {"docker (docker-compose.yml)", SECRETS_REALTIME_DOCKER_COMPOSE_FILE_PATH},
                {"ssh (id_rsa)", SECRETS_REALTIME_ID_RSA_FILE_PATH},
        };
    }

    @Test(dataProvider = "secretsSourceFiles", description = "Verify Secrets realtime scan runs successfully and detects exposed secrets in a file")
    public void verifySecretsRealtimeScanDetectsSecrets(String sourceType, String sourceFilePath) throws Exception {
        ExtentTest test = getTestLogger();
        String command = String.format("scan secrets-realtime -s %s", sourceFilePath);

        try {
            Logger.info("Validating Secrets realtime scan for source type: " + sourceType, test);
            JsonNode root = RealtimeScanUtils.runRealtimeScanCommand(command, test);

            Assert.assertTrue(root.isArray() && !root.isEmpty(),
                    "Expected Secrets realtime scan to detect at least one secret for " + sourceType + ".\nCLI Output:\n" + root);

            int secretsCount = root.size();
            Assert.assertTrue(secretsCount >= 1,
                    "Expected Secrets realtime scan to detect at least one secret for " + sourceType + ".\nCLI Output:\n" + root);
            RealtimeScanUtils.logVulnerabilityCount(secretsCount, test);
            Logger.pass("Secrets realtime scan (" + sourceType + ") completed successfully and detected " + secretsCount + " secret(s)", test);
        } catch (Exception e) {
            Logger.fail("Secrets realtime scan test failed for " + sourceType + ": " + e.getMessage(), test);
            Assert.fail("Secrets realtime scan test failed", e);
        }
    }
}
