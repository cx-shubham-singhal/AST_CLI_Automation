package com.myorg.cxone.tests.realtimescan;

import com.aventstack.extentreports.ExtentTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.myorg.cxone.helpers.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.Base;
import utils.RealtimeScanUtils;

import static com.myorg.cxone.helpers.TestConstants.ASCA_SQL_INJECTION_FILE_PATH;

public class AscaScanTest extends Base {

    @Test(description = "Verify ASCA scan runs successfully and detects vulnerabilities in a single file")
    public void verifyAscaScanDetectsVulnerabilities() throws Exception {
        ExtentTest test = getTestLogger();
        String command = String.format("scan asca --file-source %s", ASCA_SQL_INJECTION_FILE_PATH);

        try {
            JsonNode root = RealtimeScanUtils.runRealtimeScanCommand(command, test);

            Assert.assertTrue(root.path("status").asBoolean(false),
                    "ASCA scan did not report success status.\nCLI Output:\n" + root);
            Assert.assertEquals(root.path("message").asText(), "Scan successful",
                    "Unexpected ASCA scan message.\nCLI Output:\n" + root);

            JsonNode scanDetails = root.path("scan_details");
            Assert.assertTrue(scanDetails.isArray() && !scanDetails.isEmpty(),
                    "Expected ASCA scan to detect at least one vulnerability.\nCLI Output:\n" + root);

            int vulnerabilityCount = scanDetails.size();
            RealtimeScanUtils.logVulnerabilityCount(vulnerabilityCount, test);
            Logger.pass("ASCA scan completed successfully and detected " + vulnerabilityCount + " vulnerability(ies)", test);
        } catch (Exception e) {
            Logger.fail("ASCA scan test failed: " + e.getMessage(), test);
            Assert.fail("ASCA scan test failed", e);
        }
    }
}
