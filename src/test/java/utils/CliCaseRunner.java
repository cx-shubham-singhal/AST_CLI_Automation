package utils;

import com.aventstack.extentreports.ExtentTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.cxone.helpers.Logger;
import org.testng.Assert;
import org.testng.SkipException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.myorg.cxone.helpers.TestConstants.PROJECT_PATH_ZIP;
import static com.myorg.cxone.helpers.TestConstants.SCA_RESOLVER_PATH;

/** Runs a {@link CliCase} through {@link CLIHelper} and asserts exit status and output. */
public final class CliCaseRunner {

    /** Projects created by negative cases (some CLI validations fail only after the project is created). */
    public static final String PROJECT_PREFIX = "CLI_Neg_";
    public static final String REALTIME_ROOT = "src/main/resources/Phoenix-RealtimeGoat/Phoenix-RealtimeGoat";
    public static final String ZERO_UUID = "00000000-0000-0000-0000-000000000000";

    private static final Pattern EXIT = Pattern.compile("Command exited with code: (\\d+)");
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Path TMP = createTmp();

    private CliCaseRunner() {
    }

    private static Path createTmp() {
        try {
            Path dir = Files.createTempDirectory("cx-cases-");
            Files.writeString(dir.resolve("bad.txt"), "not a source");
            dir.toFile().deleteOnExit();
            return dir;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create temp folder for CLI cases", e);
        }
    }

    public static Path tmp() {
        return TMP;
    }

    /** Creates a new, empty, uniquely named file in the temp folder. */
    public static Path newTempFile(String prefix, String suffix) {
        try {
            return Files.createTempFile(TMP, prefix, suffix);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String resolve(String command) {
        String c = command;
        if (c.contains("{P}")) {
            c = c.replace("{P}", PROJECT_PREFIX + Utils.uniqueSuffix());
        }
        if (c.contains("{SCA}")) {
            if (SCA_RESOLVER_PATH == null || SCA_RESOLVER_PATH.isBlank()) {
                throw new SkipException("SCA_RESOLVER_PATH is not set");
            }
            c = c.replace("{SCA}", SCA_RESOLVER_PATH);
        }
        if (c.contains("{CFG}")) {
            c = c.replace("{CFG}", newTempFile("cfg", ".yaml").toString());
        }
        return c.replace("{Z}", PROJECT_PATH_ZIP)
                .replace("{RG}", REALTIME_ROOT)
                .replace("{BAD}", TMP.resolve("bad.txt").toString())
                .replace("{ZERO}", ZERO_UUID)
                .replace("{TMP}", TMP.toString());
    }

    public static int exitCode(String output) {
        Matcher m = EXIT.matcher(output);
        int code = 0;
        while (m.find()) {
            code = Integer.parseInt(m.group(1));
        }
        return code;
    }

    // ---- version of the cx binary under test ---------------------------------------------------------------

    private static final Pattern VERSION = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
    private static volatile String cachedVersion;

    /** Version of the cx binary under test (e.g. 2.3.65), read once. */
    public static String cliVersion() {
        if (cachedVersion == null) {
            synchronized (CliCaseRunner.class) {
                if (cachedVersion == null) {
                    try {
                        Matcher m = VERSION.matcher(Utils.stripAnsi(CLIHelper.runCommand("version")));
                        cachedVersion = m.find() ? m.group() : "0.0.0";
                    } catch (Exception e) {
                        cachedVersion = "0.0.0";
                    }
                }
            }
        }
        return cachedVersion;
    }

    /** Negative if a &lt; b, 0 if equal, positive if a &gt; b. Missing parts count as 0. */
    public static int compareVersions(String a, String b) {
        int[] x = versionParts(a);
        int[] y = versionParts(b);
        for (int i = 0; i < 3; i++) {
            if (x[i] != y[i]) {
                return Integer.compare(x[i], y[i]);
            }
        }
        return 0;
    }

    private static int[] versionParts(String v) {
        int[] parts = new int[3];
        String[] s = v.trim().split("\\.");
        for (int i = 0; i < 3 && i < s.length; i++) {
            String digits = s[i].replaceAll("\\D", "");
            parts[i] = digits.isEmpty() ? 0 : Integer.parseInt(digits);
        }
        return parts;
    }

    /** Skips the case when the cx binary under test is outside the case's since/until range. */
    public static void checkVersionRange(CliCase c) {
        String version = cliVersion();
        if (!c.since.isEmpty() && compareVersions(version, c.since) < 0) {
            throw new SkipException(c.id + " applies from CLI " + c.since + " (running " + version + ")");
        }
        if (!c.until.isEmpty() && compareVersions(version, c.until) > 0) {
            throw new SkipException(c.id + " applies up to CLI " + c.until + " (running " + version + ")");
        }
    }

    public static String run(CliCase c, ExtentTest test) {
        checkVersionRange(c);
        try {
            String command = resolve(c.command);
            Logger.info("Running CLI command: cx " + command, test);
            String output = Utils.stripAnsi(CLIHelper.runCommand(command));
            Logger.info("CLI Output:\n" + output, test);
            assertCase(c, output);
            Logger.pass(c + " - behaved as expected", test);
            return output;
        } catch (SkipException e) {
            throw e;
        } catch (AssertionError e) {
            Logger.fail(c + " failed: " + e.getMessage(), test);
            throw e;
        } catch (Exception e) {
            Logger.fail(c + " threw: " + e.getMessage(), test);
            Assert.fail(c + " threw an exception", e);
            return "";
        }
    }

    public static void assertCase(CliCase c, String output) {
        int exit = exitCode(output);
        switch (c.exit) {
            case ZERO -> Assert.assertEquals(exit, 0, c.id + ": expected exit code 0.\n" + output);
            case NON_ZERO -> Assert.assertNotEquals(exit, 0, c.id + ": expected a non-zero exit code.\n" + output);
            default -> { }
        }
        for (String s : c.contains) {
            Assert.assertTrue(matches(output, s), c.id + ": expected output to contain:\n" + s + "\n\nActual output:\n" + output);
        }
        for (String s : c.notContains) {
            Assert.assertFalse(matches(output, s), c.id + ": output must not contain:\n" + s + "\n\nActual output:\n" + output);
        }
    }

    private static boolean matches(String output, String expected) {
        if (expected.startsWith("re:")) {
            return Pattern.compile(expected.substring(3), Pattern.DOTALL).matcher(output).find();
        }
        return output.contains(expected);
    }

    /** Deletes every project whose name starts with the prefix (best effort, never throws). */
    public static synchronized void deleteProjectsWithPrefix(String prefix) {
        try {
            String out = CLIHelper.runCommand("project list --format json --filter \"limit=500\"");
            int start = out.indexOf('[');
            int end = out.lastIndexOf(']');
            if (start < 0 || end < start) {
                return;
            }
            JsonNode projects = MAPPER.readTree(out.substring(start, end + 1));
            for (JsonNode p : projects) {
                String name = p.path("Name").asText("");
                if (name.startsWith(prefix)) {
                    CLIHelper.runCommand("project delete --project-id " + p.path("ID").asText());
                }
            }
        } catch (Exception ignored) {
            // cleanup is best effort
        }
    }
}
