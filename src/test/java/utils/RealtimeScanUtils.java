package utils;

import com.aventstack.extentreports.ExtentTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.cxone.helpers.Logger;

public class RealtimeScanUtils {

    public static JsonNode runRealtimeScanCommand(String command, ExtentTest test) throws Exception {
        Logger.info("Running CLI command: cx " + command, test);
        String result = CLIHelper.runCommand(command);
        Logger.info("CLI Output:\n" + result, test);
        return new ObjectMapper().readTree(result);
    }

    public static void logVulnerabilityCount(int count, ExtentTest test) {
        Logger.info("Number of vulnerabilities detected: " + count, test);
    }
}
