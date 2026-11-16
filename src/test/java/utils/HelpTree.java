package utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Walks the cx command tree through `--help`, so tests do not need a hand-written list of commands. */
public final class HelpTree {

    /** Hidden commands do not appear in any COMMANDS list, so they are listed here (one command path per line). */
    public static final Path HIDDEN_COMMANDS = Path.of("src/test/resources/hidden-commands.txt");

    private static final Pattern SUBCOMMAND = Pattern.compile("^\\s{2}([a-z][\\w-]*):\\s+.*$");
    private static final Pattern SECTION_HEADER = Pattern.compile("^[A-Z][A-Z ]+$");
    private static volatile List<String> cached;

    private HelpTree() {
    }

    /** Every command path (e.g. "scan create"), root excluded, in discovery order. Discovered once per JVM. */
    public static List<String> allCommands() {
        if (cached == null) {
            synchronized (HelpTree.class) {
                if (cached == null) {
                    cached = discover();
                }
            }
        }
        return cached;
    }

    private static List<String> discover() {
        Set<String> found = new LinkedHashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        queue.add("");
        while (!queue.isEmpty()) {
            String path = queue.poll();
            for (String child : subcommands(help(path))) {
                String childPath = path.isEmpty() ? child : path + " " + child;
                if (found.add(childPath)) {
                    queue.add(childPath);
                }
            }
        }
        found.addAll(hiddenCommands());
        return new ArrayList<>(found);
    }

    /** Output of `cx <path> --help` (the root help when the path is empty), including the exit-code line when it failed. */
    public static String help(String path) {
        try {
            String command = path.isEmpty() ? "--help" : path + " --help";
            return Utils.stripAnsi(CLIHelper.runCommand(command));
        } catch (Exception e) {
            throw new IllegalStateException("Cannot run cx " + path + " --help", e);
        }
    }

    /** The names listed under the COMMANDS heading of a help text. */
    public static List<String> subcommands(String helpText) {
        List<String> names = new ArrayList<>();
        boolean inCommands = false;
        for (String line : helpText.split("\\r?\\n")) {
            if (line.trim().equals("COMMANDS")) {
                inCommands = true;
                continue;
            }
            if (inCommands && SECTION_HEADER.matcher(line.trim()).matches()) {
                break;
            }
            if (inCommands) {
                Matcher m = SUBCOMMAND.matcher(line);
                if (m.matches()) {
                    names.add(m.group(1));
                }
            }
        }
        return names;
    }

    public static List<String> hiddenCommands() {
        try {
            if (!Files.exists(HIDDEN_COMMANDS)) {
                return List.of();
            }
            return Files.readAllLines(HIDDEN_COMMANDS, StandardCharsets.UTF_8).stream()
                    .map(String::trim).filter(l -> !l.isEmpty() && !l.startsWith("#")).toList();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
