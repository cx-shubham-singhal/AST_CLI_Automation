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
                {"bower (bower.json)", OSS_REALTIME_BOWER_MANIFEST_FILE_PATH},
                {"gradle (build.gradle)", OSS_REALTIME_GRADLE_MANIFEST_FILE_PATH},
                {"gradle kts (build.gradle.kts)", OSS_REALTIME_GRADLE_KTS_MANIFEST_FILE_PATH},
                {"sbt (build.sbt)", OSS_REALTIME_SBT_MANIFEST_FILE_PATH},
                {"sbt (dependencies.sbt)", OSS_REALTIME_SBT_DEPENDENCIES_MANIFEST_FILE_PATH},
                {"rubygems (Gemfile)", OSS_REALTIME_GEMFILE_MANIFEST_FILE_PATH},
                {"go (go.mod - additional)", OSS_REALTIME_GO_ADDITIONAL_MANIFEST_FILE_PATH},
                {"gradle (libs.versions.toml)", OSS_REALTIME_GRADLE_LIBS_VERSIONS_MANIFEST_FILE_PATH},
                {"npm (package.json - additional)", OSS_REALTIME_NPM_ADDITIONAL_MANIFEST_FILE_PATH},
                {"maven (pom.xml - additional)", OSS_REALTIME_MAVEN_ADDITIONAL_MANIFEST_FILE_PATH},
                {"pypi (pyproject.toml)", OSS_REALTIME_PYPROJECT_MANIFEST_FILE_PATH},
                {"pypi (requirement.txt)", OSS_REALTIME_REQUIREMENT_MANIFEST_FILE_PATH},
                {"pypi (requirements-dev.txt)", OSS_REALTIME_REQUIREMENTS_DEV_MANIFEST_FILE_PATH},
                {"pypi (requirements-eq.txt)", OSS_REALTIME_REQUIREMENTS_EQ_MANIFEST_FILE_PATH},
                {"pypi (requirements-flags.txt)", OSS_REALTIME_REQUIREMENTS_FLAGS_MANIFEST_FILE_PATH},
                {"pypi (requirements-markers.txt)", OSS_REALTIME_REQUIREMENTS_MARKERS_MANIFEST_FILE_PATH},
                {"pypi (requirements-prod.txt)", OSS_REALTIME_REQUIREMENTS_PROD_MANIFEST_FILE_PATH},
                {"pypi (requirements-uv.txt)", OSS_REALTIME_REQUIREMENTS_UV_MANIFEST_FILE_PATH},
                {"pypi (requirements.txt - additional)", OSS_REALTIME_REQUIREMENTS_ADDITIONAL_MANIFEST_FILE_PATH},
                {"pypi (setup.cfg)", OSS_REALTIME_SETUP_CFG_MANIFEST_FILE_PATH},
                {"pypi (setup.py)", OSS_REALTIME_SETUP_PY_MANIFEST_FILE_PATH},
                {"nuget (app.csproj)", OSS_REALTIME_NUGET_CSPROJ_MANIFEST_FILE_PATH},
                {"packagist (composer.json)", OSS_REALTIME_COMPOSER_MANIFEST_FILE_PATH},
                {"sbt (plugins.sbt)", OSS_REALTIME_SBT_PLUGINS_MANIFEST_FILE_PATH},
                {"pypi (requirement-test.txt)", OSS_REALTIME_REQUIREMENT_TEST_MANIFEST_FILE_PATH},
                {"sbt (scalajs.sbt)", OSS_REALTIME_SCALAJS_MANIFEST_FILE_PATH},
                {"sbt (comments-only-build.sbt)", OSS_REALTIME_SBT_COMMENTS_ONLY_MANIFEST_FILE_PATH},
                {"sbt (empty-build.sbt)", OSS_REALTIME_SBT_EMPTY_MANIFEST_FILE_PATH},
                {"pypi (requirements-urls.txt)", OSS_REALTIME_REQUIREMENTS_URLS_MANIFEST_FILE_PATH},
                {"pypi (requirements-vcs.txt)", OSS_REALTIME_REQUIREMENTS_VCS_MANIFEST_FILE_PATH},
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

            Assert.assertTrue(vulnerabilityCount >= 1,
                    "Expected OSS realtime scan to detect at least one vulnerability.\nCLI Output:\n" + root);

            RealtimeScanUtils.logVulnerabilityCount(vulnerabilityCount, test);
            Logger.pass("OSS realtime scan (" + manifestType + ") completed successfully and detected " + vulnerabilityCount + " vulnerability(ies)", test);
        } catch (Exception e) {
            Logger.fail("OSS realtime scan test failed for " + manifestType + ": " + e.getMessage(), test);
            Assert.fail("OSS realtime scan test failed", e);
        }
    }
}
