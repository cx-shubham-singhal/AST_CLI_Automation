package com.myorg.cxone.tests.realtimescan;

import com.aventstack.extentreports.ExtentTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.myorg.cxone.helpers.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.Base;
import utils.RealtimeScanUtils;

import static com.myorg.cxone.helpers.TestConstants.CONTAINERS_REALTIME_FILE_PATH;

public class ContainersRealtimeScanTest extends Base {

    @Test(description = "Verify Containers realtime scan runs successfully and detects vulnerable images in a containers file")
    public void verifyContainersRealtimeScanDetectsVulnerabilities() throws Exception {
        ExtentTest test = getTestLogger();
        String command = String.format("scan containers-realtime --file-source %s", CONTAINERS_REALTIME_FILE_PATH);

        try {
            JsonNode root = RealtimeScanUtils.runRealtimeScanCommand(command, test);

            JsonNode images = root.path("Images");
            Assert.assertTrue(images.isArray() && !images.isEmpty(),
                    "Containers realtime scan did not return any images.\nCLI Output:\n" + root);

            int vulnerabilityCount = 0;
            for (JsonNode image : images) {
                vulnerabilityCount += image.path("Vulnerabilities").size();
            }

            Assert.assertTrue(vulnerabilityCount > 0,
                    "Expected Containers realtime scan to detect at least one vulnerability.\nCLI Output:\n" + root);

            RealtimeScanUtils.logVulnerabilityCount(vulnerabilityCount, test);
            Logger.pass("Containers realtime scan completed successfully and detected " + vulnerabilityCount + " vulnerability(ies)", test);
        } catch (Exception e) {
            Logger.fail("Containers realtime scan test failed: " + e.getMessage(), test);
            Assert.fail("Containers realtime scan test failed", e);
        }
    }
}
