package utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/** Loads {@link CliCase} rows from src/test/resources/cli-cases/*.tsv and checks them for mistakes and duplicates. */
public final class CliCaseLoader {

    public static final Path CASES_DIR = Path.of("src/test/resources/cli-cases");

    private CliCaseLoader() {
    }

    public static List<CliCase> loadAll() {
        List<CliCase> cases = new ArrayList<>();
        try (Stream<Path> files = Files.list(CASES_DIR)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".tsv")).sorted().toList()) {
                cases.addAll(load(file));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read CLI case files from " + CASES_DIR.toAbsolutePath(), e);
        }
        return cases;
    }

    public static List<CliCase> load(Path file) throws IOException {
        List<CliCase> cases = new ArrayList<>();
        int lineNo = 0;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            lineNo++;
            if (line.isBlank() || line.startsWith("#") || line.startsWith("id\t")) {
                continue;
            }
            try {
                cases.add(CliCase.fromRow(line.split("\t", -1), file.getFileName().toString()));
            } catch (RuntimeException e) {
                throw new IllegalStateException(file.getFileName() + " line " + lineNo + ": " + e.getMessage(), e);
            }
        }
        return cases;
    }

    /** Problems that must be fixed (the lint test fails on these). */
    public static List<String> lintErrors(List<CliCase> cases) {
        List<String> errors = new ArrayList<>();
        Map<String, CliCase> ids = new HashMap<>();
        Map<String, CliCase> commands = new HashMap<>();
        for (CliCase c : cases) {
            String where = c.sourceFile + ": " + c.id;
            if (c.id.isEmpty()) {
                errors.add(c.sourceFile + ": a case has no id (" + c.description + ")");
            } else if (ids.put(c.id, c) != null) {
                errors.add(where + " - duplicate id (also in " + ids.get(c.id).sourceFile + ")");
            }
            if (c.command.isEmpty()) {
                errors.add(where + " - empty command");
            } else {
                String key = c.command + "|" + c.exit;
                CliCase other = commands.put(key, c);
                if (other != null) {
                    errors.add(where + " - same command and exit as " + other.id + ": cx " + c.command);
                }
            }
            if (c.exit == CliCase.Exit.NON_ZERO && c.contains.length == 0) {
                errors.add(where + " - a case expecting failure must name the message it expects");
            }
            if (!c.group.equals("sanity") && !c.group.equals("regression")) {
                errors.add(where + " - group must be sanity or regression, was '" + c.group + "'");
            }
            if (!c.since.isEmpty() && !c.until.isEmpty() && CliCaseRunner.compareVersions(c.since, c.until) > 0) {
                errors.add(where + " - since is later than until");
            }
        }
        return errors;
    }

    /**
     * Likely overlaps (same command shape and same expected message). Reported, but they do not fail the build,
     * because boundary variants (limit=abc / limit=-1) are intentional.
     */
    public static List<String> lintWarnings(List<CliCase> cases) {
        Map<String, List<CliCase>> byShape = new HashMap<>();
        for (CliCase c : cases) {
            if (c.contains.length == 0) {
                continue;
            }
            byShape.computeIfAbsent(shape(c), k -> new ArrayList<>()).add(c);
        }
        Set<String> warnings = new LinkedHashSet<>();
        for (List<CliCase> group : byShape.values()) {
            if (group.size() > 1) {
                StringBuilder ids = new StringBuilder();
                group.forEach(c -> ids.append(c.id).append(' '));
                warnings.add("possible overlap (same flags, same expected message): " + ids.toString().trim());
            }
        }
        return new ArrayList<>(warnings);
    }

    private static String shape(CliCase c) {
        List<String> parts = new ArrayList<>();
        String[] words = c.command.split("\\s+");
        for (int i = 0; i < words.length; i++) {
            String w = words[i];
            if (w.startsWith("--") || i < 2) {
                parts.add(w.replaceAll("=.*", ""));
            }
        }
        return String.join(" ", parts) + "|" + String.join(";;", c.contains);
    }
}
