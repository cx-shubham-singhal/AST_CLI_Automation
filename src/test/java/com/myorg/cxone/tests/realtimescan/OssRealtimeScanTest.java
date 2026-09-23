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

public class OssRealtimeScanTest extends Base {

    @DataProvider(name = "ossManifestFiles")
    public Object[][] ossManifestFiles() {
        return new Object[][]{
                {"npm (package.json)", OSS_REALTIME_NPM_MANIFEST_FILE_PATH},
                {"go (go.mod)", OSS_REALTIME_GO_MANIFEST_FILE_PATH},
                {"maven (pom.xml)", OSS_REALTIME_MAVEN_MANIFEST_FILE_PATH},
                {"nuget (packages.config)", OSS_REALTIME_NUGET_MANIFEST_FILE_PATH},
                {"python (requirements.txt)", OSS_REALTIME_PYTHON_MANIFEST_FILE_PATH},
        };
    }

    @Test(dataProvider = "ossManifestFiles", description = "Verify OSS realtime scan runs successfully and detects vulnerable packages in a manifest file")
    public void verifyOssRealtimeScanDetectsVulnerabilities(String manifestType, String manifestFilePath) throws Exception {
        ExtentTest test = getTestLogger();
        String command = String.format("scan oss-realtime -s %s", manifestFilePath);

        try {
            Logger.info("Validating OSS realtime scan for manifest type: " + manifestType, test);
            JsonNode root = RealtimeScanUtils.runRealtimeScanCommand(command, test);

            JsonNode packages = root.path("Packages");
            Assert.assertTrue(packages.isArray() && !packages.isEmpty(),
                    "OSS realtime scan did not return any packages.\nCLI Output:\n" + root);

            int vulnerabilityCount = 0;
            for (JsonNode pkg : packages) {
                vulnerabilityCount += pkg.path("Vulnerabilities").size();
            }

            Assert.assertTrue(vulnerabilityCount > 0,
                    "Expected OSS realtime scan to detect at least one vulnerability.\nCLI Output:\n" + root);

            RealtimeScanUtils.logVulnerabilityCount(vulnerabilityCount, test);
            Logger.pass("OSS realtime scan (" + manifestType + ") completed successfully and detected " + vulnerabilityCount + " vulnerability(ies)", test);
        } catch (Exception e) {
            Logger.fail("OSS realtime scan test failed for " + manifestType + ": " + e.getMessage(), test);
            Assert.fail("OSS realtime scan test failed", e);
        }
    }
}
