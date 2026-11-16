package utils;

import java.util.Arrays;

/**
 * One data-driven CLI case, loaded from a row of src/test/resources/cli-cases/*.tsv.
 *
 * Columns (tab separated): id, area, description, command, exit, contains, notContains, since, until, group
 * <ul>
 *   <li>exit: 0 | nonzero | any</li>
 *   <li>contains / notContains: several values separated by ";;". A value starting with "re:" is a regular expression.</li>
 *   <li>since / until: optional CLI versions (e.g. 2.3.65) between which the case applies; outside it the case is skipped.</li>
 *   <li>group: sanity | regression (sanity cases are also part of the regression run).</li>
 * </ul>
 * Placeholders usable in the command are resolved by {@link CliCaseRunner}:
 * {P} unique project name, {Z} Phoenix-RealtimeGoat.zip, {RG} fixture root, {TMP} per-run temp folder,
 * {BAD} a text file that is not a valid source, {CFG} a fresh empty config file, {ZERO} the all-zero UUID,
 * {SCA} the ScaResolver executable (case is skipped when SCA_RESOLVER_PATH is not set).
 */
public final class CliCase {

    public enum Exit { ZERO, NON_ZERO, ANY }

    public static final int COLUMNS = 10;

    public final String id;
    public final String area;
    public final String description;
    public final String command;
    public final Exit exit;
    public final String[] contains;
    public final String[] notContains;
    public final String since;
    public final String until;
    public final String group;
    public final String sourceFile;

    private CliCase(String[] c, String sourceFile) {
        this.id = c[0];
        this.area = c[1];
        this.description = c[2];
        this.command = c[3];
        this.exit = parseExit(c[4]);
        this.contains = split(c[5]);
        this.notContains = split(c[6]);
        this.since = c[7];
        this.until = c[8];
        this.group = c[9].isEmpty() ? "regression" : c[9];
        this.sourceFile = sourceFile;
    }

    /** Builds a case from one TSV row; missing trailing columns are treated as empty. */
    public static CliCase fromRow(String[] columns, String sourceFile) {
        String[] c = new String[COLUMNS];
        for (int i = 0; i < COLUMNS; i++) {
            c[i] = i < columns.length ? columns[i].trim() : "";
        }
        return new CliCase(c, sourceFile);
    }

    private static Exit parseExit(String value) {
        return switch (value.toLowerCase()) {
            case "0", "zero", "pass" -> Exit.ZERO;
            case "nonzero", "non-zero", "fail" -> Exit.NON_ZERO;
            case "any", "" -> Exit.ANY;
            default -> throw new IllegalArgumentException("Unknown exit value '" + value + "' (use 0, nonzero or any)");
        };
    }

    private static String[] split(String cell) {
        if (cell == null || cell.isBlank()) {
            return new String[0];
        }
        return Arrays.stream(cell.split(";;")).map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);
    }

    public boolean isSanity() {
        return "sanity".equalsIgnoreCase(group);
    }

    /** Shown as the data-row label in the TestNG/Extent reports. */
    @Override
    public String toString() {
        return id + " " + description;
    }
}
