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

public class AscaScanTest extends Base {

    @DataProvider(name = "ascaSourceFiles")
    public Object[][] ascaSourceFiles() {
        return new Object[][]{
                {"python (sql_injection.py)", ASCA_SQL_INJECTION_FILE_PATH},
                {"javascript (JavaScript.js)", ASCA_JAVASCRIPT_FILE_PATH},
        };
    }

    @Test(dataProvider = "ascaSourceFiles", description = "Verify ASCA scan runs successfully and detects vulnerabilities in a single file")
    public void verifyAscaScanDetectsVulnerabilities(String sourceType, String sourceFilePath) throws Exception {
        ExtentTest test = getTestLogger();
        String command = String.format("scan asca --file-source %s", sourceFilePath);

        try {
            Logger.info("Validating ASCA scan for source type: " + sourceType, test);
            JsonNode root = RealtimeScanUtils.runRealtimeScanCommand(command, test);

            Assert.assertTrue(root.path("status").asBoolean(false),
                    "ASCA scan did not report success status for " + sourceType + ".\nCLI Output:\n" + root);
            Assert.assertEquals(root.path("message").asText(), "Scan successful",
                    "Unexpected ASCA scan message for " + sourceType + ".\nCLI Output:\n" + root);

            JsonNode scanDetails = root.path("scan_details");
            Assert.assertTrue(scanDetails.isArray() && !scanDetails.isEmpty(),
                    "Expected ASCA scan to detect at least one vulnerability for " + sourceType + ".\nCLI Output:\n" + root);

            int vulnerabilityCount = scanDetails.size();
            Assert.assertTrue(vulnerabilityCount >= 1,
                    "Expected ASCA scan to detect at least one vulnerability for " + sourceType + ".\nCLI Output:\n" + root);
            RealtimeScanUtils.logVulnerabilityCount(vulnerabilityCount, test);
            Logger.pass("ASCA scan (" + sourceType + ") completed successfully and detected " + vulnerabilityCount + " vulnerability(ies)", test);
        } catch (Exception e) {
            Logger.fail("ASCA scan test failed for " + sourceType + ": " + e.getMessage(), test);
            Assert.fail("ASCA scan test failed", e);
        }
    }
}
