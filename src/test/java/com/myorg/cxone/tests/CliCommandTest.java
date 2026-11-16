package com.myorg.cxone.tests;

import com.aventstack.extentreports.ExtentTest;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import utils.Base;
import utils.CliCase;
import utils.CliCaseLoader;
import utils.CliCaseRunner;

import java.util.List;

/**
 * Runs every row of src/test/resources/cli-cases/*.tsv (validation errors, negative paths, formats, completion...).
 * To add a case, add a row to the matching .tsv file (or a new one): no Java is needed.
 * Pass -Dcases.group=sanity (or the suite parameter "cases.group") to run only the sanity rows.
 */
public class CliCommandTest extends Base {

    @DataProvider(name = "cliCases", parallel = true)
    public Object[][] cliCases(ITestContext context) {
        String group = context.getCurrentXmlTest().getParameter("cases.group");
        if (group == null || group.isBlank()) {
            group = System.getProperty("cases.group", "");
        }
        final boolean sanityOnly = group.trim().equalsIgnoreCase("sanity");
        List<CliCase> cases = CliCaseLoader.loadAll().stream()
                .filter(c -> !sanityOnly || c.isSanity())
                .toList();
        Object[][] rows = new Object[cases.size()][1];
        for (int i = 0; i < cases.size(); i++) {
            rows[i][0] = cases.get(i);
        }
        return rows;
    }

    @Test(groups = {"sanity"}, dataProvider = "cliCases", description = "Data-driven CLI command case (src/test/resources/cli-cases)")
    public void verifyCliCommand(CliCase cliCase) {
        ExtentTest test = getTestLogger();
        CliCaseRunner.run(cliCase, test);
    }

    /** Some validations fail only after the project was created; remove every CLI_Neg_* project the cases left behind. */
    @AfterClass(alwaysRun = true)
    public void removeProjectsCreatedByCases() {
        CliCaseRunner.deleteProjectsWithPrefix(CliCaseRunner.PROJECT_PREFIX);
    }
}
