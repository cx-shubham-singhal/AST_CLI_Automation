package com.myorg.cxone.tests;

import com.aventstack.extentreports.ExtentTest;
import com.myorg.cxone.helpers.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.Base;
import utils.CLIHelper;
import utils.CliCaseRunner;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Multi-step CLI flows that need more than one command or a look at the file the CLI wrote (single-command checks live in
 * cli-cases/*.tsv). Every flow works on throw-away files, so the machine's real CLI configuration (~/.checkmarx) is never
 * touched and these tests are safe to run in parallel with the others.
 */
public class CliWorkflowTest extends Base {

    @Test(description = "configure set writes the property to the given config file only")
    public void configureSetWritesToCustomConfigFile() throws Exception {
        ExtentTest test = getTestLogger();
        Path cfg = CliCaseRunner.newTempFile("cfg", ".yaml");
        String output = run("configure set --prop-name cx_tenant --prop-value probe_tenant --config-file-path " + cfg, test);

        Assert.assertTrue(output.contains("Setting property [ cx_tenant ] to value [ probe_tenant ]"), output);
        String content = Files.readString(cfg);
        Assert.assertTrue(content.contains("cx_tenant: probe_tenant"), "Config file should contain the new tenant.\n" + content);
        Logger.pass("Property was written to the custom config file", test);
    }

    @Test(description = "configure set masks a secret value in its output")
    public void configureSetMasksSecrets() throws Exception {
        ExtentTest test = getTestLogger();
        Path cfg = CliCaseRunner.newTempFile("cfg", ".yaml");
        String secret = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEF";
        // the secret is deliberately not written to the report
        String output = CLIHelper.runCommand("configure set --prop-name cx_apikey --prop-value " + secret + " --config-file-path " + cfg);
        Logger.info("CLI Output:\n" + output, test);

        Assert.assertEquals(CliCaseRunner.exitCode(output), 0, output);
        Assert.assertTrue(output.contains("******CDEF"), "Only the last 4 characters should be shown.\n" + output);
        Assert.assertFalse(output.contains(secret), "The full secret must not be echoed.\n" + output);
        Logger.pass("API key is masked in the command output", test);
    }

    @Test(description = "ignore-vulnerability adds, de-duplicates and removes an OSS entry")
    public void ignoreVulnerabilityAddRepeatRemove() throws Exception {
        ExtentTest test = getTestLogger();
        Path data = CliCaseRunner.newTempFile("ignore-data", ".json");
        Files.writeString(data, "{\"PackageManager\":\"npm\",\"PackageName\":\"lodash\",\"PackageVersion\":\"4.17.20\"}");
        Path ignoreFile = CliCaseRunner.newTempFile("ignored", ".json");
        Files.deleteIfExists(ignoreFile);
        String base = "ignore-vulnerability --scan-type oss --data @" + data + " --ignored-file-path " + ignoreFile;

        String added = run(base, test);
        Assert.assertTrue(added.contains("ignored 1 vulnerability(ies); 1 entry now in"), "First add should ignore one entry.\n" + added);

        String repeated = run(base, test);
        Assert.assertTrue(repeated.contains("ignored 0 vulnerability(ies); 1 entry now in"), "Adding the same entry again must be idempotent.\n" + repeated);

        String removed = run(base + " --remove", test);
        Assert.assertTrue(removed.contains("revived 1 vulnerability(ies); 0 entries now in"), "--remove should revive the entry.\n" + removed);
        Logger.pass("ignore-vulnerability add / repeat / remove behave as expected", test);
    }

    /** Runs a command that must succeed and returns its output. */
    private String run(String command, ExtentTest test) throws Exception {
        Logger.info("Running CLI command: cx " + command, test);
        String output = CLIHelper.runCommand(command);
        Logger.info("CLI Output:\n" + output, test);
        Assert.assertEquals(CliCaseRunner.exitCode(output), 0, "Command should succeed.\n" + output);
        return output;
    }
}
