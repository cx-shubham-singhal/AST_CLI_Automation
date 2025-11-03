package utils;

import com.aventstack.extentreports.ExtentTest;
import com.myorg.cxone.helpers.Logger;
import org.testng.ITestResult;
import org.testng.Reporter;

import java.io.*;
import java.util.regex.Pattern;

public class CLIHelper {

    private static String getCliCommand() {
        String cliPath = System.getenv("CX_CLI_PATH");
        if (cliPath == null || cliPath.isEmpty()) {
            return "cx"; // fallback to PATH
        }
        // Ensure it points to the executable, not just folder
        if (!cliPath.endsWith("cx.exe")) {
            cliPath = cliPath + "\\cx.exe";
        }
        return cliPath;
    }

    public static String runCommand(String command) throws Exception {
        String cliPath = getCliCommand();

        // Wrap path in quotes if it contains spaces
        if (cliPath.contains(" ")) {
            cliPath = "\"" + cliPath + "\"";
        }

        ITestResult result = Reporter.getCurrentTestResult();
        if (result != null) {
            result.setAttribute("cliCommand","cx " + command);
        }

        // Split command into array to avoid quoting issues
        String[] cmd = {"cmd.exe", "/c", cliPath + " " + command};

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            output.append("\nCommand exited with code: ").append(exitCode);
        }

        return output.toString();
    }

    public static String runCommandUntilPattern(String command, String stopPattern, ExtentTest test) throws Exception {
        String cliPath = getCliCommand();

        if (cliPath.contains(" ")) {
            cliPath = "\"" + cliPath + "\"";
        }

        ITestResult result = Reporter.getCurrentTestResult();
        if (result != null) {
            result.setAttribute("cliCommand","cx " + command);
        }

        String[] cmd = {"cmd.exe", "/c", cliPath + " " + command};

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        Pattern pattern = Pattern.compile(stopPattern);

        while ((line = reader.readLine()) != null) {
            Logger.info(line, test);
            output.append(line).append("\n");

            // Stop when the desired pattern is reached
            if (pattern.matcher(line).find()) {
                break;
            }
        }

        return output.toString();
    }


    public static String runCommandWithTimeout(String command, int timeoutMillis) throws Exception {
        String cliPath = getCliCommand();

        if (cliPath.contains(" ")) {
            cliPath = "\"" + cliPath + "\"";
        }

        ITestResult result = Reporter.getCurrentTestResult();
        if (result != null) {
            result.setAttribute("cliCommand","cx " + command);
        }

        String[] cmd = {"cmd.exe", "/c", cliPath + " " + command};

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();

        long startTime = System.currentTimeMillis();
        String line;
        while ((System.currentTimeMillis() - startTime) < timeoutMillis) {
            if (reader.ready() && (line = reader.readLine()) != null) {
                output.append(line).append("\n");

                // Optional early exit if guide is printed
                if (line.contains("Setup guide:")) {
                    break;
                }
            } else {
                Thread.sleep(100); // Avoid tight loop
            }
        }

        process.destroyForcibly(); // Ensure the interactive process doesn't hang
        return output.toString();
    }

}
