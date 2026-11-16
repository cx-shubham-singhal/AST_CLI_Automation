package com.myorg.cxone.tests;

import com.aventstack.extentreports.ExtentTest;
import com.myorg.cxone.helpers.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.Base;
import utils.CliCase;
import utils.CliCaseLoader;
import utils.HelpTree;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Checks the quality of the test suite itself, without needing a tenant:
 * the case files must be free of duplicates, and a report shows which CLI flags no test uses yet.
 */
public class SuiteQualityTest extends Base {

    private static final Pattern FLAG = Pattern.compile("--([a-z0-9][a-z0-9-]*)");
    private static final Pattern HEADER = Pattern.compile("^[A-Z][A-Z ]+$");

    @Test(groups = {"sanity"}, description = "CLI case files have no duplicate ids or commands and every failing case names its message")
    public void caseFilesAreConsistent() {
        ExtentTest test = getTestLogger();
        List<CliCase> cases = CliCaseLoader.loadAll();
        Logger.info("Loaded " + cases.size() + " CLI cases", test);

        for (String warning : CliCaseLoader.lintWarnings(cases)) {
            Logger.info("WARNING " + warning, test);
        }
        List<String> errors = CliCaseLoader.lintErrors(cases);
        Assert.assertTrue(errors.isEmpty(), "Problems in src/test/resources/cli-cases:\n - " + String.join("\n - ", errors));
        Logger.pass(cases.size() + " cases checked, no duplicates or malformed rows", test);
    }

    /**
     * Never fails: writes test-output/flag-coverage.txt. A flag counts as covered when its --name appears in the case files,
     * the Java tests or the scan data sheet. Flags come from the live --help output of every command.
     */
    @Test(description = "Report CLI flags that no test uses")
    public void reportUntestedFlags() throws IOException {
        ExtentTest test = getTestLogger();

        Set<String> used = usedFlags();
        Set<String> global = new TreeSet<>();
        Map<String, Set<String>> perCommand = new TreeMap<>();
        // flags come from the live --help output of every command, so there is nothing stored to keep up to date
        for (String command : HelpTree.allCommands()) {
            parseFlags(HelpTree.help(command), command, global, perCommand);
        }

        Set<String> all = new TreeSet<>(global);
        perCommand.values().forEach(all::addAll);
        Set<String> untested = new TreeSet<>(all);
        untested.removeAll(used);

        List<String> lines = new ArrayList<>();
        lines.add("CLI flag coverage - " + (all.size() - untested.size()) + " of " + all.size() + " flags are mentioned by at least one test");
        lines.add("(a flag counts as covered when its --name appears in the case files, Java tests or the scan data sheet)");
        lines.add("");
        Set<String> globalUntested = new TreeSet<>(global);
        globalUntested.removeAll(used);
        lines.add("Global flags not used anywhere (" + globalUntested.size() + "/" + global.size() + "): " + globalUntested);
        lines.add("");
        lines.add("Command-specific flags not used anywhere:");
        for (Map.Entry<String, Set<String>> e : perCommand.entrySet()) {
            Set<String> missing = new TreeSet<>(e.getValue());
            missing.removeAll(used);
            if (!missing.isEmpty()) {
                lines.add(String.format("  cx %-32s %d/%d  %s", e.getKey(), missing.size(), e.getValue().size(), missing));
            }
        }
        Path report = Path.of("test-output/flag-coverage.txt");
        Files.createDirectories(report.getParent());
        Files.write(report, lines, StandardCharsets.UTF_8);

        Logger.info(String.join("\n", lines), test);
        Logger.pass((all.size() - untested.size()) + "/" + all.size() + " flags covered; report: " + report, test);
    }

    private static void parseFlags(String help, String command, Set<String> global, Map<String, Set<String>> perCommand) {
        String section = "";
        for (String line : help.split("\\r?\\n")) {
            String t = line.trim();
            if (HEADER.matcher(t).matches()) {
                section = t;
                continue;
            }
            if (section.equals("GLOBAL FLAGS") || section.equals("FLAGS")) {
                Matcher m = FLAG.matcher(line);
                if (m.find() && !m.group(1).equals("help")) {
                    if (section.equals("GLOBAL FLAGS")) {
                        global.add(m.group(1));
                    } else {
                        perCommand.computeIfAbsent(command, k -> new TreeSet<>()).add(m.group(1));
                    }
                }
            }
        }
    }

    private static Set<String> usedFlags() throws IOException {
        Set<String> used = new TreeSet<>();
        List<Path> textFiles = new ArrayList<>();
        for (String dir : new String[]{"src/test/resources/cli-cases", "src/test/java", "src/main/java"}) {
            Path p = Path.of(dir);
            if (Files.isDirectory(p)) {
                try (Stream<Path> walk = Files.walk(p)) {
                    walk.filter(f -> (f.toString().endsWith(".tsv") || f.toString().endsWith(".java")) && !f.getFileName().toString().equals("SuiteQualityTest.java")).forEach(textFiles::add);
                }
            }
        }
        for (Path f : textFiles) {
            collect(Files.readString(f, StandardCharsets.UTF_8), used);
        }
        Path xlsx = Path.of("src/main/resources/ScanTestData.xlsx");
        if (Files.exists(xlsx)) {
            DataFormatter fmt = new DataFormatter();
            try (InputStream in = Files.newInputStream(xlsx); Workbook wb = new XSSFWorkbook(in)) {
                for (Sheet sheet : wb) {
                    for (Row row : sheet) {
                        for (Cell cell : row) {
                            collect(fmt.formatCellValue(cell), used);
                        }
                    }
                }
            }
        }
        return used;
    }

    private static void collect(String text, Set<String> into) {
        Matcher m = FLAG.matcher(text);
        while (m.find()) {
            into.add(m.group(1));
        }
    }
}
